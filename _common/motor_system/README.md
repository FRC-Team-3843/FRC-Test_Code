# Motor System Common Copy

Legacy copy snapshot of the generic motor abstraction, setup, tuning, and characterization stack extracted from `Motor_System`.

## Status
- Preferred path: depend on the internal library modules in `Motor_System`
- Snapshot status: retained as a fallback copy source while the library workflow is proven on more projects

## Intended Use
Use this only if a project cannot yet consume the shared `Motor_System` modules directly.

## Preferred Guidance
1. Keep `Motor_System` as the source of truth
2. Reuse its library modules: `motor-core`, `motor-tune`, `motor-test`, and `motor-dashboard`
3. Treat this snapshot as temporary compatibility material, not the main update path

## Notes
- This snapshot may drift if it is not refreshed intentionally.
- Update `Motor_System` first. Only refresh this snapshot when you explicitly need copy-based reuse.
