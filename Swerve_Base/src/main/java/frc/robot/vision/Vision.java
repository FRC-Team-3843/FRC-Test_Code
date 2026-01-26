package frc.robot.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Constants;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.targeting.PhotonTrackedTarget;
import swervelib.SwerveDrive;

/**
 * PhotonVision integration scaffold. Add camera entries in Cameras enum.
 * Vision can be disabled via Constants.VisionConstants.ENABLE_VISION.
 */
public class Vision {
  private static final Alert layoutAlert =
      new Alert("Missing AprilTag layout file in deploy directory", AlertType.kWarning);

  private final AprilTagFieldLayout fieldLayout;
  private final Supplier<Pose2d> currentPose;
  private final double maxLatencySeconds;

  public Vision(Supplier<Pose2d> currentPose, Object field, double maxLatencySeconds) {
    this.currentPose = currentPose;
    this.maxLatencySeconds = maxLatencySeconds;
    this.fieldLayout = loadFieldLayout();

    if (fieldLayout != null) {
      for (Cameras camera : Cameras.values()) {
        camera.init(fieldLayout);
      }
    }
  }

  private AprilTagFieldLayout loadFieldLayout() {
    Path deployPath = Filesystem.getDeployDirectory().toPath()
        .resolve(Constants.VisionConstants.APRILTAG_LAYOUT_FILE);
    try {
      layoutAlert.set(false);
      return new AprilTagFieldLayout(deployPath);
    } catch (IOException e) {
      layoutAlert.set(true);
      return null;
    }
  }

  public void updatePoseEstimation(SwerveDrive swerveDrive) {
    if (fieldLayout == null) {
      return;
    }

    for (Cameras camera : Cameras.values()) {
      Optional<EstimatedRobotPose> poseEst = camera.getEstimatedGlobalPose();
      if (poseEst.isEmpty()) {
        continue;
      }
      var pose = poseEst.get();
      double now = Timer.getFPGATimestamp();
      if (now - pose.timestampSeconds > maxLatencySeconds) {
        continue;
      }
      swerveDrive.addVisionMeasurement(
          pose.estimatedPose.toPose2d(),
          pose.timestampSeconds,
          camera.curStdDevs);
    }
  }

  enum Cameras {
    // Add cameras here when hardware is ready.
    // Example:
    // FRONT_CAM("front",
    //     new Rotation3d(0, Math.toRadians(-15), 0),
    //     new Translation3d(0.3, 0.0, 0.2),
    //     VecBuilder.fill(4, 4, 8),
    //     VecBuilder.fill(0.5, 0.5, 1));
    ;

    public final PhotonCamera camera;
    public final Rotation3d robotToCamRotation;
    public final Translation3d robotToCamTranslation;
    public final Matrix<N3, N1> singleTagStdDevs;
    public final Matrix<N3, N1> multiTagStdDevs;
    public Matrix<N3, N1> curStdDevs;
    public PhotonPoseEstimator poseEstimator;

    Cameras(String name,
            Rotation3d robotToCamRotation,
            Translation3d robotToCamTranslation,
            Matrix<N3, N1> singleTagStdDevs,
            Matrix<N3, N1> multiTagStdDevs) {
      camera = new PhotonCamera(name);
      this.robotToCamRotation = robotToCamRotation;
      this.robotToCamTranslation = robotToCamTranslation;
      this.singleTagStdDevs = singleTagStdDevs;
      this.multiTagStdDevs = multiTagStdDevs;
      this.curStdDevs = singleTagStdDevs;
    }

    public void init(AprilTagFieldLayout layout) {
      poseEstimator = new PhotonPoseEstimator(
          layout,
          PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
          new Transform3d(robotToCamTranslation, robotToCamRotation));
      // Strategy set in constructor now
    }

    public Optional<EstimatedRobotPose> getEstimatedGlobalPose() {
      if (poseEstimator == null) {
        return Optional.empty();
      }
      // Using update() which is deprecated but works for now.
      // Replacement is dependent on strategy which is constructor arg.
      // If I change constructor, I change this.
      // For now, removing @SuppressWarnings and leaving warnings.
      Optional<EstimatedRobotPose> poseEst = poseEstimator.update(camera.getLatestResult());
      poseEst.ifPresent(this::updateEstimationStdDevs);
      return poseEst;
    }

    private void updateEstimationStdDevs(EstimatedRobotPose estimatedPose) {
      var targets = estimatedPose.targetsUsed;
      int numTags = 0;
      double avgDist = 0.0;

      for (PhotonTrackedTarget target : targets) {
        var tagPose = poseEstimator.getFieldTags().getTagPose(target.getFiducialId());
        if (tagPose.isEmpty()) {
          continue;
        }
        numTags++;
        avgDist += tagPose.get().toPose2d().getTranslation()
            .getDistance(estimatedPose.estimatedPose.toPose2d().getTranslation());
      }

      if (numTags == 0) {
        curStdDevs = singleTagStdDevs;
        return;
      }

      avgDist /= numTags;
      Matrix<N3, N1> stdDevs = singleTagStdDevs;
      if (numTags > 1) {
        stdDevs = multiTagStdDevs;
      }
      if (numTags == 1 && avgDist > 4.0) {
        stdDevs = VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
      } else {
        stdDevs = stdDevs.times(1 + (avgDist * avgDist / 30.0));
      }
      curStdDevs = stdDevs;
    }
  }
}