import subprocess
import time

versions = ["1","2"]
mat_sizes = range(600, 3400, 400)

def parse_line(line):
    if(line==""):
        return
    return line.split(":")


for version in versions:
    print(f"Testing {version}-by-{version} version...")
    for size in mat_sizes:
        print(f"Matrix size: {size}x{size}")
        cmd = f"g++ -O2 matrixproduct.cpp -o fileout -lpapi && ./fileout {version} {size}"
        result = subprocess.run(cmd, shell=True, capture_output=True)
        output_str = result.stdout.decode("utf-8")

        output_lines = output_str.split("\n")

        # Parse the output and store the measures
        measures = dict()
        for line in output_lines:
            # Replace the following line with your own code to parse the output
            measure = parse_line(line)
            if measure is not None:
                [factor,measurement] = measure
                measures[factor]=measurement
        print(measures)
    print()