	#include <stdio.h>
	#include <iostream>
	#include <iomanip>
	#include <time.h>
	#include <cstdlib>
	#include <papi.h>
	#include <string.h>
	using namespace std;

	#define SYSTEMTIME clock_t

	
	void OnMult(int m_ar, int m_br) 
	{
		
		SYSTEMTIME Time1, Time2;
		
		char st[100];
		double temp;
		int i, j, k;

		double *pha, *phb, *phc;
		

			
		pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
		phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
		phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

		for(i=0; i<m_ar; i++)
			for(j=0; j<m_ar; j++)
				pha[i*m_ar + j] = (double)1.0;



		for(i=0; i<m_br; i++)
			for(j=0; j<m_br; j++)
				phb[i*m_br + j] = (double)(i+1);



		Time1 = clock();

		for(i=0; i<m_ar; i++)
		{	for( j=0; j<m_br; j++)
			{	temp = 0;
				for( k=0; k<m_ar; k++)
				{	
					temp += pha[i*m_ar+k] * phb[k*m_br+j];
				}
				phc[i*m_ar+j]=temp;
			}
		}


		Time2 = clock();
		sprintf(st, "Time:%3.3f\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
		cout << st;

	/*
		// display 10 elements of the result matrix tto verify correctness
		cout << "Result matrix: " << endl;
		for(i=0; i<1; i++)
		{	for(j=0; j<min(10,m_br); j++)
				cout << phc[j] << " ";
		}
		cout << endl;
	*/
		free(pha);
		free(phb);
		free(phc);
		
		
	}

	// add code here for line x line matriz multiplication
	void  OnMultLine(double* pha, double* phb, double* phc, int m_1_cols, int m_1_lines, int m_2_cols,int m_2_lines,int i_offset=0,int j_offset=0,int k_offset=0,int bkSize=INT32_MAX/2,bool print_info=true)
	{
		SYSTEMTIME Time1, Time2;
		
		if(m_1_cols != m_2_lines)
			throw "Number of columns of the first matrix has to be equal to the number of lines of the second matrix";

		char st[100];
		double temp;
		int i, j, k;



		Time1 = clock();

		for(i=i_offset; i<min(i_offset + bkSize, m_1_lines); i++){	
			for(k=k_offset; k < min(k_offset + bkSize, m_1_cols); k++){	
				temp = 0;
				for(j=j_offset; j< min(j_offset + bkSize, m_2_cols); j++){	 
					temp += pha[i*m_1_cols+k] * phb[k*m_2_cols+j];
				}
				phc[i*m_2_cols+k]+=temp;
			}
		}


		
		Time2 = clock();
		if(print_info){
			sprintf(st, "Time:%3.3f\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
			cout << st;
		}

		
		// display 10 elements of the result matrix tto verify correctness

		/*
		cout << "Result matrix: " << endl;
		for(i=i_offset; i<min(i_offset + bkSize, m_1_lines); i++){	
			for(j=j_offset; j< min(j_offset + bkSize, m_2_cols); j++){	 
				cout << phc[i*m_2_cols + j] << " ";
			}
			cout << endl;
		}
		

		cout << endl;
		*/
		
		

	}

	// add code here for block x block matriz multiplication
	void OnMultBlock(double* pha, double* phb,double* phc,int m_1_cols, int m_1_lines, int m_2_cols,int m_2_lines, int bkSize) {
		
		if (m_1_cols % bkSize != 0 && m_1_lines % bkSize != 0 && m_2_cols % bkSize != 0 && m_2_lines % bkSize != 0)
			cout << "Invalid Block Size" << endl;

		// Check if the dimensions of the matrices are compatible for multiplication
		if (m_1_cols != m_2_lines) {
			cout << "Error: Incompatible matrix dimensions for multiplication" << endl;
			return;
		}

		// Allocate memory for the result matrix
		SYSTEMTIME Time1, Time2;
		char st[100];
		Time1 = clock();
		// Perform block-by-block matrix multiplication
		for (int i = 0; i < m_1_lines; i += bkSize) {
			for (int k = 0; k < m_1_cols; k += bkSize) {
				for (int j = 0; j < m_2_cols; j += bkSize) {
					// Multiply the current block of matrices
					OnMultLine(pha,phb,phc,m_1_cols,m_1_lines,m_2_cols,m_2_lines,i,j,k,bkSize,false);
				}
			}
		}
		Time2 = clock();
		sprintf(st, "Time:%3.3f\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
		cout << st;

	/*
		// Print the result matrix
		for (int i = 0; i < m_1_lines; ++i) {
			for (int j = 0; j < m_2_cols; ++j) {
				cout << phc[i * m_2_cols + j] << " ";
			}
			cout << endl;
		}
	*/	
	}	


	void handle_error (int retval)
	{
	printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
	exit(1);
	}

	void init_papi() {
	int retval = PAPI_library_init(PAPI_VER_CURRENT);
	if (retval != PAPI_VER_CURRENT && retval < 0) {
		printf("PAPI library version mismatch!\n");
		exit(1);
	}
	if (retval < 0) handle_error(retval);

	std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
				<< " MINOR: " << PAPI_VERSION_MINOR(retval)
				<< " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
	}


	int main (int argc, char *argv[])
	{

		char c;
		int lin, col, blockSize;
		int op;

		if(argc!=3 and argc!=4)
			cerr << "Wrong number of arguments " << argc << endl;
		
		int EventSet = PAPI_NULL;
		long long values[4];
		int ret;
		

		ret = PAPI_library_init( PAPI_VER_CURRENT );
		if ( ret != PAPI_VER_CURRENT )
			std::cout << "FAIL" << endl;


		ret = PAPI_create_eventset(&EventSet);
			if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


		ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
		if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


		ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
		if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

		ret = PAPI_add_event(EventSet, PAPI_L2_DCA);
		if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCA" << endl;

		ret = PAPI_add_event(EventSet, PAPI_DP_OPS);
		if (ret != PAPI_OK) cout << "ERROR: PAPI_DP_OPS" << endl;


		//"1. Multiplication"
		//"2. Line Multiplication"
		//"3. Block Multiplication"

		op = stoi(argv[1]);

		//printf("Dimensions: lins=cols ? ");
		lin = stoi(argv[2]);
		col = lin;

		int i,j;

		double* pha = (double *)malloc((lin * col) * sizeof(double));
		double* phb = (double *)malloc((lin * col) * sizeof(double));
		double* phc = (double *)malloc((lin * col) * sizeof(double));

		for(i=0; i<lin; i++)
			for(j=0; j<lin; j++)
				pha[i*lin + j] = (double)1.0;



		for(i=0; i<col; i++)
			for(j=0; j<col; j++)
				phb[i*col + j] = (double)(i+1);

		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		switch (op){
			case 1: 
				OnMult(lin, col);
				break;
			case 2:
				OnMultLine(pha,phb,phc,lin,col,lin, col);  
				break;
			case 3:
				blockSize = stoi(argv[3]);
				OnMultBlock(pha,phb,phc,lin,col,lin,col,blockSize);  
				break;
			default:
				break;
		}

		ret = PAPI_stop(EventSet, values);
		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
		printf("L1 DCM:%lld\n",values[0]);
		printf("L2 DCM:%lld\n",values[1]);
		printf("L2 DCA:%lld\n", values[2]);
		printf("L2 DCH:%lld\n", values[2]-values[1]);
		printf("DOUBLE PRECISION FLOPS:%lld\n", values[3]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 

		free(pha);
		free(phb);
		free(phc);


		ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event" << endl; 

		ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event" << endl; 
		ret = PAPI_remove_event( EventSet, PAPI_L2_DCA );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event" << endl; 
		ret = PAPI_remove_event( EventSet, PAPI_DP_OPS );
		if ( ret != PAPI_OK )
			std::cout << "FAIL remove event" << endl; 

		ret = PAPI_destroy_eventset( &EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL destroy" << endl;

	}