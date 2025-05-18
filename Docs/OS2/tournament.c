#include "user/user.h"

int main(int argc, char *argv[]) {
  if(argc != 2) exit(1);
  int N = atoi(argv[1]);
  int id = tournament_create(N);
  if(id < 0) { printf("create failed\n"); exit(1); }
  if(tournament_acquire() < 0) { exit(1); }

  printf("PID %d finished the tournament!\n", getpid());

  if(tournament_release() < 0) { exit(1); }
  

  if (id == 0) {
    for (int i = 1; i < N; i++) {
      wait(0);  // clean up child process
    }
    tournament_destroy();
  }
  
  exit(0);
}
