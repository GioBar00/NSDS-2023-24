#include <mpi/mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/**
 * Group number:
 *
 * Group members
 * Member 1
 * Member 2
 * Member 3
 *
 **/

// Set DEBUG 1 if you want car movement to be deterministic
#define DEBUG 0

const int num_segments = 256;

const int num_iterations = 1000;
const int count_every = 10;

const double alpha = 0.5;
const int max_in_per_sec = 10;


// Returns the number of car that enter the first segment at a given iteration.
int create_random_input() {
#if DEBUG
    return 1;
#else
    return rand() % max_in_per_sec;
#endif
}

// Returns 1 if a car needs to move to the next segment  at a given iteration, 0 otherwise.
int move_next_segment() {
#if DEBUG
    return 1;
#else
    return rand() < alpha ? 1 : 0;
#endif
}

int main(int argc, char** argv) { 
    MPI_Init(NULL, NULL);

    int rank;
    int num_procs;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
    srand(time(NULL) + rank);
    int size = num_segments / num_procs;
    int cars_in_segment[size];
    memset(cars_in_segment, 0, num_segments / num_procs * sizeof(int));

    // Simulate for num_iterations iterations
    for (int it = 0; it < num_iterations; ++it) {
        // Cars may exit from the last segment
        int exiting_cars = 0;
        int entering_cars;

        // Check last segment
        for(int i = 0; i < cars_in_segment[size - 1]; i++){
            if(move_next_segment()) {
                exiting_cars++;
            }
        }

        // Move cars across segment
        for(int i = size - 2; i >= 0; i--){
            for(int j = 0; j < cars_in_segment[i]; j++){
                if(move_next_segment()) {
                    cars_in_segment[i + 1]++;
                    cars_in_segment[i]--;
                }
            }
        }
        cars_in_segment[size - 1] -= exiting_cars;

        if (rank != num_procs - 1 )
            MPI_Send(&exiting_cars, 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);

        if (rank != 0)
            MPI_Recv(&entering_cars, 1, MPI_INT, rank - 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

        //New cars may enter in the first segment of each process
        if(rank == 0)
            entering_cars = create_random_input();

        cars_in_segment[0] += entering_cars;

        // When needed, compute the overall sum
        if (it%count_every == 0) {
            int global_sum = 0;
            int moving_cars = 0;
            for(int i = 0; i < size; i++)
                moving_cars += cars_in_segment[i];

            MPI_Reduce(&moving_cars, &global_sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
            if (rank == 0) {
                printf("Iteration: %d, sum: %d\n", it, global_sum);
            }
        }

        MPI_Barrier(MPI_COMM_WORLD);
    }

    MPI_Finalize();
}
