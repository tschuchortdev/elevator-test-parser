import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Option
import java.io.File
import java.nio.charset.Charset
import org.apache.commons.cli.Options

class App {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			val cliOptions = Options()
			cliOptions.addOption(Option("in", true, "input file paths").apply { setArgs(Option.UNLIMITED_VALUES) })
			cliOptions.addOption(Option("out", true, "output directory path"))
			val cliArgs = GnuParser().parse(cliOptions, args)

			val inputPaths =
					if(cliArgs.hasOption("in"))
						cliArgs.getOptionValue("in").split(" ")
					else
						emptyList()

			val outputPath =
					if(cliArgs.hasOption("out"))
						cliArgs.getOptionValue("out")
					else
						null

			val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

			for (inputPath in inputPaths) {
				val inputFile = File(inputPath)

				if(inputFile.isDirectory)
					throw IllegalArgumentException("input paths must be files not directories: $inputPath")
				else if(!inputFile.isFile)
					throw IllegalArgumentException("input file not found: $inputPath")

				val testCase: TestCase = mapper.readValue(inputFile)
				testCase.validate()

				val outputFile = File(
						(outputPath?.suffix(File.separator)?.plus(inputFile.name) ?: inputFile.canonicalPath)
								.removeSuffix(".yml") + ".txt")

				outputFile.createNewFile()
				outputFile.writeText(testCase.toTxt(), Charset.defaultCharset())
			}
		}
	}
}

fun TestCase.validate() {
	floors.forEach { (height, _, _) ->
		if(height < 1)
			throw IllegalArgumentException("height must be at least 0")
	}

	elevators.forEach { (speed, maxLoad, startFloor) ->
		if(speed < 1)
			throw IllegalArgumentException("speed must be at least 1")

		if(maxLoad <= 0)
			throw IllegalArgumentException("maxLoad must be bigger than 0")

		if (startFloor < 1)
			throw IllegalArgumentException("out of bounds: floor indices start at 1")

		if (startFloor > floors.size)
			throw IllegalArgumentException("out of bounds: floor indeces must not be bigger than number of floors")
	}

	persons.forEach { (startTime, startFloor, destinationFloor, maxWait, weight) ->
		if(startTime < 0)
			throw IllegalArgumentException("startTime must be at least 0")

		if (startFloor < 1 || destinationFloor < 1)
			throw IllegalArgumentException("out of bounds: floor indices start at 1")

		if (startFloor > floors.size || destinationFloor > floors.size)
			throw IllegalArgumentException("out of bounds: floor indeces must not be bigger than number of floors")

		if(maxWait < 1)
			throw IllegalArgumentException("maxWait must be bigger than 0")

		if(weight < 0)
			throw IllegalArgumentException("weight must be at least 0")
	}
}

fun TestCase.toTxt(): String {
	val newLine = System.lineSeparator()

	return floors.mapIndexed { index, floor ->
		val id = index + 1
		val floorBelowId =
				if(index > 0)
					"1${id - 1}"
				else
					"0"
		val floorAboveId =
				if (index != floors.lastIndex)
					"1${id + 1}"
				else
					"0"

		val interfaceName = if (floor.hasUpDownButton) "UpDownButton" else "Interface"

		//IDs of the elevators that stop on this floor
		val accesibleElevatorIds = floor.elevators.map { "3$it" }

		//floor IDs get prefix 1, floor interfaces get prefix 2AA
		"Floor { 1$id $floorBelowId $floorAboveId ${floor.height} 1 2$id } $newLine" +

		//each floor gets one interface that can call all specified elevators
		accesibleElevatorIds.joinToString(
				prefix = "$interfaceName { 2$id ${accesibleElevatorIds.size} ",
				postfix = " } $newLine",
				separator = " ")
	}.concat() +

	elevators.mapIndexed { index, (speed, maxLoad, startFloor) ->
		val id = index + 1

		//Ids of the floors that this elevator stops on
		val accessibleFloors = floors
				.withIndex()
				.filter { (_, floor) ->
					floor.elevators.contains(id)
				}
				.map { (index, _) ->
					index + 1
				}

		//elevators get prefix 3
		accessibleFloors.map { "4${id}0$it" }.joinToString(
				prefix = "Elevator { 3$id $speed $maxLoad 1$startFloor ${accessibleFloors.size} ",
				postfix = " } $newLine",
				separator = " ") +

		//each elevator gets many interfaces to call the floors that were specified in the "floors:" list
		//elevator interfaces get prefix 4
		accessibleFloors.map { "Interface { 4${id}0$it 1 1$it } $newLine" }.concat()
	}.concat() +

	persons.mapIndexed { index, (startTime, startFloor, destinationFloor, maxWait, weight) ->
		val id = index + 1
		//persons get prefix 5
		"Person { 5$id 1$startFloor 1$destinationFloor $maxWait $weight $startTime } $newLine"
	}.concat()
}

fun List<String>.concat() = fold("", String::plus)

fun String.suffix(s: String) = removeSuffix(s) + s