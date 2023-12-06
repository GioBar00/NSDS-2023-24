#include <mpi/mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

int rank;
int num_procs;

const int num_rounds = 1000;

const int min_num = 1;
const int max_num = 1000;

// Array, one element per process
// The leader board, instantiated and used in process 0
int *leaderboard = NULL;

// Array, one element per process
// The array of number selected in the current round
int *selected_numbers = NULL;

// The leader for the current round
int leader = 0;

// Allocate dynamic variables
void allocate_vars() {
    leaderboard = (int*)calloc(num_procs, sizeof(int));
    selected_numbers = (int*)malloc(sizeof(int)*num_procs);
}

// Deallocate dynamic variables
void free_vars() {
    free(leaderboard);
    free(selected_numbers);
}

// Select a random number between min_num and max_num
int select_number() {
  return min_num + rand() % (max_num - min_num + 1);
}

// Function used to communicate the selected number to the leader
void send_num_to_leader(int num) {
    int guess = select_number();
    MPI_Gather(&guess, 1, MPI_INT, selected_numbers, 1, MPI_INT, leader, MPI_COMM_WORLD);
}

// Compute the winner (-1 if there is no winner)
// Function invoked by the leader only
int compute_winner(int number_to_guess) {
    int winner;
    int min_distance = 2147483647;
    int draw = 0;

    for (int i=0; i<num_procs; i++) {
        int distance = abs(selected_numbers[i] - number_to_guess);
        if (distance == min_distance) {
            draw = 1;
        }
        else if (distance < min_distance) {
            min_distance = distance;
            winner = i;
            draw = 0;
        }
    }
    if (draw)
        winner = leader;
    return winner;
}

// Function used to communicate the winner to everybody
void send_winner(int *winner) {
    MPI_Bcast(winner, 1, MPI_INT, leader, MPI_COMM_WORLD);
}

// Update leader
void update_leader(int winner) {
    leader = winner;
}

// Update leaderboard (invoked by process 0 only)
void update_leaderboard(int winner) {
    leaderboard[winner]++;
}

// Print the leaderboard
void print_leaderboard(int round, int winner) {
    printf("\n* Round %d *\n", round);
    printf("Winner: %d\n", winner);
    printf("Leaderboard\n");
    for (int i=0; i<num_procs; i++) {
        printf("P%d:\t%d\n", i, leaderboard[i]);
    }
}

int main(int argc, char** argv) { 
    MPI_Init(NULL, NULL);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
    srand(time(NULL) + rank);

    allocate_vars();

    for (int round=0; round<num_rounds; round++) {
        int selected_number = select_number();
        send_num_to_leader(selected_number);

        int winner;
        if (rank == leader) {
            int num_to_guess = select_number();
            winner = compute_winner(num_to_guess);
        }
        send_winner(&winner);
        update_leader(winner);

        if (rank == 0) {
            update_leaderboard(winner);
            print_leaderboard(round, winner);
        }
    }

    MPI_Barrier(MPI_COMM_WORLD);
    free_vars();

    MPI_Finalize();
}
