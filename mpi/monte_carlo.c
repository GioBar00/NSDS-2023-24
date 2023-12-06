#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi/mpi.h>
#include <math.h>

const int num_iter_per_proc = 10 * 1000 * 1000;

int main() {
  MPI_Init(NULL, NULL);
    
  int rank;
  int num_procs;
  int sum;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  srand(time(NULL) + rank);

  // TODO
  int local_sum = 0;
  double x, y;
  double x_2, y_2;
  /*! Monte carlo computation*/
  for(int i = 0; i < num_iter_per_proc; i++){
      x = (double)rand() / (double)RAND_MAX;
      y = (double)rand() / (double)RAND_MAX;
      x_2 = x*x;
      y_2 = y*y;
      if(x_2 + y_2 <= 1) {
          local_sum++;
      }
  }

  MPI_Reduce(&local_sum, &sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
  
  if (rank == 0) {
      printf("I got total sum %d ", sum);
      double pi = (4.0*sum) / (num_iter_per_proc*num_procs);
      printf("Pi = %f\n", pi);
  }

  MPI_Finalize();
  return 0;
}
