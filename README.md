## Commandline Arguments:

- `--in` list of yaml test files (not directories!) to be converted (use absolute paths to be sure)
- `--out` output directory (optional)

## Example Test File

```
floors:
  - height: 4
    elevators:
      - 1

  - height: 6
    elevators:
      - 1
      - 2
      
elevators:
  - speed: 2
    maxLoad: 10
    startFloor: 1
    
  - speed: 4
    maxLoad: 30
    startFloor: 2

persons:
  - startFloor: 1
    destinationFloor: 2
    startTime: 0
    maxWait: 50
    weight: 2

  - startFloor: 2 
    destinationFloor: 1
    startTime: 5
    maxWait: 50
    weight: 2
```

Times start at `0`. All IDs are indices starting at `1`, i.e. the bottom most floor is `1` and the first elevator is also `1`. Custom events like malfunctions are not supported as of yet.
