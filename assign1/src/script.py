import subprocess
import time
import csv

versions = [1,2]
mat_sizes = range(600, 3400, 400)
keys = ['Size','Time','L1 DCM', 'L2 DCM', 'L2 DCA', 'L2 DCH','DOUBLE PRECISION FLOPS']


def parse_line(line):
    if(line==""):
        return
    return line.split(":")


for version in versions:
    with open(f"cpp_{version if version != 2 else str(version)+'_a'}.csv", mode='w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(keys)
        for size in mat_sizes:
            measures = dict()
            measures["Size"]=size
            cmd = f"g++ -O2 matrixproduct.cpp -o fileout -lpapi && ./fileout {version} {size}"
            result = subprocess.run(cmd, shell=True, capture_output=True)
            output_str = result.stdout.decode("utf-8")

            output_lines = output_str.split("\n")
            # Parse the output and store the measures
            for line in output_lines:
                # Replace the following line with your own code to parse the output
                measure = parse_line(line)
                if measure is not None and len(measure)==2:
                    [factor,measurement] = measure
                    if(factor in keys):
                        measures[factor]=measurement
            print(measures)
            writer.writerow(list(measures.values()))


for version in versions:
    with open(f"rust_{version}.csv", mode='w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(keys)
        for size in mat_sizes:
            measures = dict()
            measures["Size"]=size
            cmd = f"rustc -O matrixproduct.rs && ./matrixproduct {version} {size}"
            result = subprocess.run(cmd, shell=True, capture_output=True)
            output_str = result.stdout.decode("utf-8")

            output_lines = output_str.split("\n")
            # Parse the output and store the measures
            for line in output_lines:
                # Replace the following line with your own code to parse the output
                measure = parse_line(line)
                if measure is not None and len(measure)==2:
                    [factor,measurement] = measure
                    if(factor in keys):
                        measures[factor]=measurement
            print(measures)
            writer.writerow(list(measures.values()))

"""
versions = [2,3]
mat_sizes = range(4096, 12288, 2048)
block_sizes = [128,256,512]

for version in versions:
    with open(f"cpp_{version if version != 2 else str(version)+'_b'}.csv", mode='w', newline='') as file:
        writer = csv.writer(file)
        if(version==3):
            keys.insert(1,"Block size")
        writer.writerow(keys)
        for size in mat_sizes:
            
            if(version==2):
                measures = dict()
                measures["Size"]=size
                cmd = f"g++ -O2 matrixproduct.cpp -o fileout -lpapi && ./fileout {version} {size}"
                result = subprocess.run(cmd, shell=True, capture_output=True)
                output_str = result.stdout.decode("utf-8")

                output_lines = output_str.split("\n")
                # Parse the output and store the measures
                for line in output_lines:
                    # Replace the following line with your own code to parse the output
                    measure = parse_line(line)
                    if measure is not None and len(measure)==2:
                        [factor,measurement] = measure
                        if(factor in keys):
                            measures[factor]=measurement
                print(measures)
                writer.writerow(list(measures.values()))
            if(version==3):
                for block_size in block_sizes:
                    measures = dict()
                    measures["Size"]=size
                    measures["Block Size"]=block_size
                    cmd = f"g++ -O2 matrixproduct.cpp -o fileout -lpapi && ./fileout {version} {size} {block_size}"
                    result = subprocess.run(cmd, shell=True, capture_output=True)
                    output_str = result.stdout.decode("utf-8")

                    output_lines = output_str.split("\n")
                    # Parse the output and store the measures
                    for line in output_lines:
                        # Replace the following line with your own code to parse the output
                        measure = parse_line(line)
                        if measure is not None and len(measure)==2:
                            [factor,measurement] = measure
                            if(factor in keys):
                                measures[factor]=measurement
                    print(measures)
                    writer.writerow(list(measures.values()))


"""