package frc.robot.auto;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants;
import frc.robot.subsystems.TankDriveSubsystem;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Choreo auto integration via reflection to keep base code flexible.
 * Update method names here if Choreo API changes in future seasons.
 */
public final class ChoreoAutos {
  private ChoreoAutos() {}

  public static Command buildAuto(String name, TankDriveSubsystem drive) {
    try {
      Class<?> choreoClass = Class.forName("choreo.Choreo");
      Object trajectory = tryGetTrajectory(choreoClass, name);
      if (trajectory == null) {
        return Commands.none();
      }

      Command command = tryBuildCommand(choreoClass, trajectory, drive);
      return command != null ? command : Commands.none();
    } catch (Exception e) {
      return Commands.none();
    }
  }

  private static Object tryGetTrajectory(Class<?> choreoClass, String name) throws Exception {
    Object trajectory = tryInvokeStatic(choreoClass, "getTrajectory", new Class<?>[] {String.class}, name);
    if (trajectory == null) {
      trajectory = tryInvokeStatic(choreoClass, "loadTrajectory", new Class<?>[] {String.class}, name);
    }

    if (trajectory instanceof Optional<?> optional) {
      return optional.orElse(null);
    }
    return trajectory;
  }

  private static Command tryBuildCommand(Class<?> choreoClass, Object trajectory, TankDriveSubsystem drive)
      throws Exception {
    PIDController xController = new PIDController(
        Constants.AutoConstants.TRANSLATION_P,
        Constants.AutoConstants.TRANSLATION_I,
        Constants.AutoConstants.TRANSLATION_D);
    PIDController yController = new PIDController(
        Constants.AutoConstants.TRANSLATION_P,
        Constants.AutoConstants.TRANSLATION_I,
        Constants.AutoConstants.TRANSLATION_D);
    PIDController thetaController = new PIDController(
        Constants.AutoConstants.ROTATION_P,
        Constants.AutoConstants.ROTATION_I,
        Constants.AutoConstants.ROTATION_D);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    Supplier<?> poseSupplier = drive::getPose;
    Object kinematics = drive.getKinematics();
    Consumer<ChassisSpeeds> speedsConsumer = drive::driveRobotRelative;

    Object[] args = new Object[] {
        trajectory,
        poseSupplier,
        kinematics,
        xController,
        yController,
        thetaController,
        speedsConsumer,
        drive
    };

    Command command = tryInvokeCommand(choreoClass, "choreoSwerveCommand", args);
    if (command != null) {
      return command;
    }
    command = tryInvokeCommand(choreoClass, "choreoCommand", args);
    if (command != null) {
      return command;
    }

    return null;
  }

  private static Object tryInvokeStatic(Class<?> type, String methodName, Class<?>[] paramTypes, Object... args)
      throws Exception {
    try {
      Method method = type.getMethod(methodName, paramTypes);
      if (!Modifier.isStatic(method.getModifiers())) {
        return null;
      }
      return method.invoke(null, args);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static Command tryInvokeCommand(Class<?> choreoClass, String methodName, Object[] args)
      throws Exception {
    for (Method method : choreoClass.getMethods()) {
      if (!method.getName().equals(methodName)) {
        continue;
      }
      if (!Command.class.isAssignableFrom(method.getReturnType())) {
        continue;
      }
      if (!Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      if (!isCompatible(method.getParameterTypes(), args)) {
        continue;
      }
      Object result = method.invoke(null, args);
      if (result instanceof Command command) {
        return command;
      }
    }
    return null;
  }

  private static boolean isCompatible(Class<?>[] paramTypes, Object[] args) {
    if (paramTypes.length != args.length) {
      return false;
    }
    for (int i = 0; i < paramTypes.length; i++) {
      Class<?> param = paramTypes[i];
      Object arg = args[i];
      if (arg == null) {
        return false;
      }
      Class<?> argClass = arg.getClass();
      if (param.isPrimitive()) {
        param = wrapPrimitive(param);
      }
      if (!param.isAssignableFrom(argClass)) {
        return false;
      }
    }
    return true;
  }

  private static Class<?> wrapPrimitive(Class<?> primitive) {
    if (primitive == boolean.class) return Boolean.class;
    if (primitive == byte.class) return Byte.class;
    if (primitive == short.class) return Short.class;
    if (primitive == int.class) return Integer.class;
    if (primitive == long.class) return Long.class;
    if (primitive == float.class) return Float.class;
    if (primitive == double.class) return Double.class;
    if (primitive == char.class) return Character.class;
    return primitive;
  }
}
