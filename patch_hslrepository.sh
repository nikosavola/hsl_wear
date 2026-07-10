sed -i 's/val query = GraphQLQueries.planRoute(/val (date, time) = GraphQLQueries.getCurrentLocalTime()\n            val query = GraphQLQueries.planRoute(/g' app/src/main/java/com/hsl/wear/data/repository/HslRepository.kt
sed -i 's/query = query/query = query,\n                variables = mapOf("date" to date, "time" to time)/g' app/src/main/java/com/hsl/wear/data/repository/HslRepository.kt
