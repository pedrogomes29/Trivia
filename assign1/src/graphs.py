import matplotlib as plt
import pandas as pd
import matplotlib.pyplot as plt
import csv

df1 = pd.read_csv('cpp_1.csv')
df2 = pd.read_csv('cpp_2_a.csv')
df3 = pd.read_csv('rust_1_pc_pedro.csv')
df4 = pd.read_csv('rust_2_pc_pedro.csv')
df5 = pd.read_csv('cpp_3.csv')
df6 = pd.read_csv('cpp_1_pc_pedro.csv')
df7 = pd.read_csv('cpp_2_a_pc_pedro.csv')

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
    plt.legend(["Naive Multiplication", "Line Multiplication"])
    plt.plot(df1[independent_variable], df1[dependent_variable], 'tab:blue') 
    plt.plot(df2[independent_variable], df2[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.savefig('images/plot1_{}.png'.format(dependent_variable))
    plt.close(fig)


for dependent_variable in df3.columns[1:]:
    fig = plt.figure()
    plt.scatter(df3[independent_variable], df3[dependent_variable])
    plt.scatter(df4[independent_variable], df4[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.legend(["Naive Multiplication", "Line Multiplication"])
    plt.plot(df3[independent_variable], df3[dependent_variable], 'tab:blue') 
    plt.plot(df4[independent_variable], df4[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.savefig('images/plot2_{}.png'.format(dependent_variable))
    plt.close(fig)



for dependent_variable in df3.columns[1:]:
    fig = plt.figure()
    plt.scatter(df6[independent_variable], df6[dependent_variable])
    plt.scatter(df3[independent_variable], df3[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.legend(["Naive Multiplication C++", "Naive Multiplication Rust"])
    plt.plot(df6[independent_variable], df6[dependent_variable], 'tab:blue') 
    plt.plot(df3[independent_variable], df3[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.savefig('images/plot3_{}.png'.format(dependent_variable))
    plt.close(fig)



for dependent_variable in df4.columns[1:]:
    fig = plt.figure()
    plt.scatter(df7[independent_variable], df7[dependent_variable])
    plt.scatter(df4[independent_variable], df4[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.legend(["Line C++", "Line Rust"])
    plt.plot(df7[independent_variable], df7[dependent_variable], 'tab:blue') 
    plt.plot(df4[independent_variable], df4[dependent_variable],'tab:orange')
    fig.canvas.mpl_connect('close_event', on_close)
    plt.savefig('images/plot4_{}.png'.format(dependent_variable))
    plt.close(fig)

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
        plt.plot(group[independent_variable], group[dependent_variable], label="Block Size: " + str(name))
        

    plt.scatter(df5[independent_variable], df5[dependent_variable])
    plt.xlabel(independent_variable)
    plt.ylabel(dependent_variable)
    plt.plot(df5[independent_variable], df5[dependent_variable], 'tab:red', label='Line Multiplication') 
    plt.legend( loc='upper left')	
    fig.canvas.mpl_connect('close_event', on_close)
    plt.savefig('images/plot5_{}.png'.format(dependent_variable))
    plt.close(fig)

"""
"""
"""
measures = [ 
{'Size': 4096, 'Block Size': 128, 'Time': '51.470', 'L1 DCM': '10204396156', 'L2 DCM': '34638931087', 'L2 DCA': '1100493932', 'L2 DCH': '-33538437155', 'DOUBLE PRECISION FLOPS': '137975824385'}
,{'Size': 4096, 'Block Size': 256, 'Time': '56.828', 'L1 DCM': '9309893283', 'L2 DCM': '23937769856', 'L2 DCA': '605417794', 'L2 DCH': '-23332352062', 'DOUBLE PRECISION FLOPS': '137707388929'}
,{'Size': 4096, 'Block Size': 512, 'Time': '64.558', 'L1 DCM': '8870683352', 'L2 DCM': '20226138352', 'L2 DCA': '358734097', 'L2 DCH': '-19867404255', 'DOUBLE PRECISION FLOPS': '137573171201'}
,{'Size': 6144, 'Block Size': 128, 'Time': '177.898', 'L1 DCM': '34430620043', 'L2 DCM': '114938579223', 'L2 DCA': '3235251083', 'L2 DCH': '-111703328140', 'DOUBLE PRECISION FLOPS': '465668407297'}
,{'Size': 6144, 'Block Size': 256, 'Time': '188.104', 'L1 DCM': '31428886659', 'L2 DCM': '79700397202', 'L2 DCA': '1766707508', 'L2 DCH': '-77933689694', 'DOUBLE PRECISION FLOPS': '464762437633'}
,{'Size': 6144, 'Block Size': 512, 'Time': '197.525', 'L1 DCM': '29966365215', 'L2 DCM': '69121528386', 'L2 DCA': '1179905542', 'L2 DCH': '-67941622844', 'DOUBLE PRECISION FLOPS': '464309452801'}
]
keys = ['Size','Block Size','Time','L1 DCM', 'L2 DCM', 'L2 DCA', 'L2 DCH','DOUBLE PRECISION FLOPS']
with open(f"cpp_3_teste.csv", mode='w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(keys)
        for measure in measures:
            writer.writerow(list(measure.values()))
"""