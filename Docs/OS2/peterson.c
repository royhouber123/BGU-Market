#include "peterson.h"

struct peterson_lock peterson_locks[MAX_PETERSON_LOCKS];

void main();

void 
peterson_init(void) {
  for (int i = 0; i < MAX_PETERSON_LOCKS; i++) {
    peterson_locks[i].used = 0;
    peterson_locks[i].flag[0] = 0;
    peterson_locks[i].flag[1] = 0;
    peterson_locks[i].turn = 0;
  }
}

int 
peterson_create(void) {
  for (int i = 0; i < MAX_PETERSON_LOCKS; i++) {
    if (__sync_lock_test_and_set(&peterson_locks[i].used, 1) == 0) {
      __sync_synchronize();
      peterson_locks[i].flag[0] = 0;
      peterson_locks[i].flag[1] = 0;
      peterson_locks[i].turn = 0;
      return i;
    }
  }
  return -1;
}

int 
peterson_acquire(int lock_id , int role) {

  // Validate arguments
  if (lock_id < 0 || lock_id >= MAX_PETERSON_LOCKS || (role != 0 && role != 1))
    return -1;

  struct peterson_lock *lock = &peterson_locks[lock_id];

  if (!lock->used)
    return -1;

  int other = 1 - role;

  lock->flag[role] = 1;
  __sync_synchronize();
  lock->turn = other;
  __sync_synchronize();

  while (lock->flag[other] && lock->turn == other) {
    yield();  // Yield the CPU voluntarily
  }
  
  return 0;
}

int 
peterson_release(int lock_id , int role) {

  if (lock_id < 0 || lock_id >= MAX_PETERSON_LOCKS || (role != 0 && role != 1))
    return -1;

  struct peterson_lock *lock = &peterson_locks[lock_id];

  if (!lock->used)
    return -1;

  __sync_synchronize();
  lock->flag[role] = 0;
  __sync_synchronize();

 return 0;
}

int 
peterson_destroy(int lock_id) {

  if (lock_id < 0 || lock_id >= MAX_PETERSON_LOCKS)
    return -1;

  struct peterson_lock *lock = &peterson_locks[lock_id];

  if (!lock->used)
    return -1;

  __sync_synchronize();
  lock->used = 0;
 return 0;
}