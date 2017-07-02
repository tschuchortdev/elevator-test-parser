typealias ElevatorIndex = Int
typealias FloorIndex = Int

data class TestCase(
		val floors: List<Floor>,
		val elevators: List<Elevator>,
		val persons: List<Person>
)

data class Floor(
		val height: Int = 1,
		val elevators: List<ElevatorIndex>
)

data class Elevator(
		val speed: Int = 1,
		val maxLoad: Int = 10,
		val startFloor: FloorIndex
)

data class Person(
		val startTime: Int,
		val startFloor: FloorIndex,
		val destinationFloor: FloorIndex,
		val maxWait: Int = 100,
		val weight: Int = 1
)