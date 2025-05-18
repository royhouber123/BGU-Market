#define MAX_PETERSON_LOCKS 15

struct peterson_lock {
  int used;           // 1 if this slot is in use
  int flag[2];        // intent to enter critical section
  int turn;           // whose turn it is
};