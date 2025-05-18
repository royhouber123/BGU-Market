#include "kernel/param.h"
#include "kernel/types.h"
#include "kernel/stat.h"
#include "user/user.h"
#include "kernel/fs.h"
#include "kernel/fcntl.h"
#include "kernel/syscall.h"
#include "kernel/memlayout.h"
#include "kernel/riscv.h"

int
main(int argc, char *argv[])
    {
    printf("Peterson Test Start 1\n");
    int lock_id = peterson_create();
    if (lock_id < 0) exit(1);

    printf("Peterson Test Start 2\n");
    int role = fork() > 0 ? 0 : 1;

    for (int i = 0; i < 100; i++) {
    peterson_acquire(lock_id, role);
    printf("Process %d in critical section\n", role);
    peterson_release(lock_id, role);
    }

    printf("Peterson Test Start 3\n");

    if (role == 0) {
    wait(0);
    peterson_destroy(lock_id);
    }

    printf("Peterson Test Start 4\n");

    exit(0);
}