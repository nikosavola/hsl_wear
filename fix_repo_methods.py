import re

with open("app/src/main/java/com/hsl/wear/data/repository/TransitRepository.kt", "r") as f:
    content = f.read()

content = content.replace("saveRouteState(newState)\n        return Result.success(newState)", "return saveRouteState(newState).map { newState }")

with open("app/src/main/java/com/hsl/wear/data/repository/TransitRepository.kt", "w") as f:
    f.write(content)
