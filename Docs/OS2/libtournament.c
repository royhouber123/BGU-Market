#include "user/user.h"
#include "peterson.h"

#define MAX_PROCESSES 16

static int  *locks   = 0;     /* Peterson lock-IDs in breadth-first order   */
static int   N       = 0;     /* number of leaf processes                   */
static int   levels  = 0;     /* tree depth: leaves are at level L-1        */
static int   index_id = -1;   /* this process’ leaf index (0 … N-1)         */

/* ---------- helpers ----------------------------------------------------- */

/* role at a given level: 0 = left child, 1 = right child                  */
static inline int
role_for_level(int lvl)
{
  return (index_id >> (levels - lvl - 1)) & 1;
}

/* breadth-first index of the internal node on our path at level <lvl>     */
static inline int
lock_for_level(int lvl)
{
  /* nodes on upper levels = 2^lvl - 1  */
  return ((1 << lvl) - 1) + (index_id >> (levels - lvl));
}

/* ---------- public API -------------------------------------------------- */

int
tournament_create(int processes)
{
  if (processes <= 0 || processes > MAX_PROCESSES ||
      (processes & (processes - 1)) != 0)
    return -1;                         /* must be power-of-two               */

  N = processes;

  /* depth L = log2(N) */
  levels = 0;
  for (int t = N; t > 1; t >>= 1)
    levels++;

  /* allocate and create Peterson locks: N-1 internal nodes                */
  int total = N - 1;
  locks = malloc(total * sizeof(int));
  if (!locks)
    return -1;

  for (int i = 0; i < total; i++) {
    locks[i] = peterson_create();
    if (locks[i] < 0)
      return -1;
  }

  /* fork leaves: parent becomes index 0, each child gets its own index    */
  index_id = 0;
  for (int i = 1; i < N; i++) {
    int pid = fork();
    if (pid < 0)  return -1;      /* fork failed                            */
    if (pid == 0) {               /* child                                 */
      index_id = i;
      break;
    }
  }

  return index_id;                /* each process learns its leaf index    */
}

/* climb bottom-up (parent-of-leaf → … → root)                              */
int
tournament_acquire(void)
{
  if (index_id < 0)  return -1;

  for (int lvl = levels - 1; lvl >= 0; --lvl) {
    int r        = role_for_level(lvl);
    int lock_idx = lock_for_level(lvl);
    if (peterson_acquire(locks[lock_idx], r) < 0)
      return -1;
  }
  
  return 0;
}

/* unwind: release root first, then walk back down to the leaves            */
int
tournament_release(void)
{
  if (index_id < 0)  return -1;

  for (int lvl = 0; lvl < levels; ++lvl) {
    int r        = role_for_level(lvl);
    int lock_idx = lock_for_level(lvl);
    if (peterson_release(locks[lock_idx], r) < 0)
      return -1;
  }
  return 0;
}

int 
tournament_destroy(void) {
  if (index_id != 0 || !locks) return -1;

  for (int i = 0; i < N - 1; i++) {
    peterson_destroy(locks[i]);
  }

  free(locks);
  locks = 0;
  return 0;
}