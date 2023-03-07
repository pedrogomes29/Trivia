import matplotlib as plt
import pandas as pd
import matplotlib.pyplot as plt
import csv

df1 = pd.read_csv('cpp_1.csv')
df2 = pd.read_csv('cpp_2_a.csv')
df3 = pd.read_csv('rust_1.csv')
df4 = pd.read_csv('rust_2.csv')
df5 = pd.read_csv('cpp_3.csv')

def on_close(event):
    print('Closed Figure!')

independent_variable = 'Size'

"""
for dependent_variable in df1.columns[1:]:
    fig = plt.figure()
    plt.scatter(df1[independent_variable], df1[dependent_variable])
    plt.scatter(df2[independent_variable], df2[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.legend(["Algorithm 1", "Algorithm 2"])
    plt.plot(df1[independent_variable], df1[dependent_variable], 'tab:blue') 
    plt.plot(df2[independent_variable], df2[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.show()


for dependent_variable in df3.columns[1:]:
    fig = plt.figure()
    plt.scatter(df3[independent_variable], df3[dependent_variable])
    plt.scatter(df4[independent_variable], df4[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.legend(["Algorithm 1", "Algorithm 2"])
    plt.plot(df3[independent_variable], df3[dependent_variable], 'tab:blue') 
    plt.plot(df4[independent_variable], df4[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.show()


for dependent_variable in df3.columns[1:]:
    fig = plt.figure()
    plt.scatter(df1[independent_variable], df1[dependent_variable])
    plt.scatter(df3[independent_variable], df3[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.legend(["C++", "Rust"])
    plt.plot(df1[independent_variable], df1[dependent_variable], 'tab:blue') 
    plt.plot(df3[independent_variable], df3[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.show()

for dependent_variable in df4.columns[1:]:
    fig = plt.figure()
    plt.scatter(df2[independent_variable], df2[dependent_variable])
    plt.scatter(df4[independent_variable], df4[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.legend(["C++", "Rust"])
    plt.plot(df2[independent_variable], df2[dependent_variable], 'tab:blue') 
    plt.plot(df4[independent_variable], df4[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.show()

"""
# Read in the CSV file and create a data frame
df5 = pd.read_csv('cpp_2_b.csv')
df6 = pd.read_csv('cpp_3.csv')

# Define the column to group by
group_column = 'Block Size'

# Group the data by the column of interest
groups = df6.groupby(group_column)

# Loop through each group and create a plot for that group

for dependent_variable in df5.columns[1:]:
    fig = plt.figure()
    for name, group in groups:
        # Create a new figure
        # Plot the data for the current group
        plt.scatter(group[independent_variable], group[dependent_variable])
        plt.plot(group[independent_variable], group[dependent_variable], label=name)
        

    plt.scatter(df5[independent_variable], df5[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.plot(df5[independent_variable], df5[dependent_variable], 'tab:red', label='Algorithm 2') 
    plt.legend( loc='upper left')	
    fig.canvas.mpl_connect('close_event', on_close)
    plt.show()

"""
measures = [{'Size': 4096, 'Block Size': 128, 'Time': '52.302', 'L1 DCM': '10202785115', 'L2 DCM': '34382345538', 'L2 DCA': '1075277551', 'L2 DCH': '-33307067987', 'DOUBLE PRECISION FLOPS': '137975824385'}
,{'Size': 4096, 'Block Size': 256, 'Time': '56.104', 'L1 DCM': '9311210115', 'L2 DCM': '23598552262', 'L2 DCA': '580138966', 'L2 DCH': '-23018413296', 'DOUBLE PRECISION FLOPS': '137707388929'}
,{'Size': 4096, 'Block Size': 512, 'Time': '58.173', 'L1 DCM': '8869295726', 'L2 DCM': '20806771466', 'L2 DCA': '363326939', 'L2 DCH': '-20443444527', 'DOUBLE PRECISION FLOPS': '137573171201'}
,{'Size': 6144, 'Block Size': 128, 'Time': '177.194', 'L1 DCM': '34450150017', 'L2 DCM': '115010194696', 'L2 DCA': '3227836150', 'L2 DCH': '-111782358546', 'DOUBLE PRECISION FLOPS': '465668407297'}
,{'Size': 6144, 'Block Size': 256, 'Time': '189.617', 'L1 DCM': '31428662381', 'L2 DCM': '80071975258', 'L2 DCA': '1787045095', 'L2 DCH': '-78284930163', 'DOUBLE PRECISION FLOPS': '464762437633'}
,{'Size': 6144, 'Block Size': 512, 'Time': '197.138', 'L1 DCM': '29966631520', 'L2 DCM': '70173580543', 'L2 DCA': '1216808984', 'L2 DCH': '-68956771559', 'DOUBLE PRECISION FLOPS': '464309452801'}
,{'Size': 8192, 'Block Size': 128, 'Time': '422.181', 'L1 DCM': '81566614709', 'L2 DCM': '271616070680', 'L2 DCA': '12488998376', 'L2 DCH': '-259127072304', 'DOUBLE PRECISION FLOPS': '1103806595073'}
,{'Size': 8192, 'Block Size': 256, 'Time': '569.877', 'L1 DCM': '74446684576', 'L2 DCM': '171917055060', 'L2 DCA': '4870850041', 'L2 DCH': '-167046205019', 'DOUBLE PRECISION FLOPS': '1101659111425'}
,{'Size': 8192, 'Block Size': 512, 'Time': '547.249', 'L1 DCM': '70939319805', 'L2 DCM': '152439345494', 'L2 DCA': '2839301971', 'L2 DCH': '-149600043523', 'DOUBLE PRECISION FLOPS': '1100585369601'}]
keys = ['Size','Block Size','Time','L1 DCM', 'L2 DCM', 'L2 DCA', 'L2 DCH','DOUBLE PRECISION FLOPS']
with open(f"cpp_3.csv", mode='w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(keys)
        for measure in measures:
            writer.writerow(list(measure.values()))
            """