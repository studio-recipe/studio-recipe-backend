package com.recipe.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 여러 작업을 동시에(같은 순간에) 시작시키고, 전부 끝날 때까지 기다렸다가
 * 각 작업의 성공/예외를 담은 Future를 돌려주는 역할만 한다.
 * 성공/실패 판정은 호출하는 쪽(테스트)의 책임이다.
 */
public final class ConcurrentRunner {

    private static final long AWAIT_TIMEOUT_SECONDS = 30;

    private ConcurrentRunner() {
    }

    public static <T> List<Future<T>> runAtTheSameMoment(List<Callable<T>> tasks) throws InterruptedException {
        int taskCount = tasks.size();
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        CountDownLatch allThreadsReady = new CountDownLatch(taskCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch allThreadsDone = new CountDownLatch(taskCount);

        List<Future<T>> futures = new ArrayList<>();
        try {
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(() -> runAfterSignal(task, allThreadsReady, startSignal, allThreadsDone)));
            }

            allThreadsReady.await();
            startSignal.countDown();
            allThreadsDone.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }
        return futures;
    }

    private static <T> T runAfterSignal(
            Callable<T> task,
            CountDownLatch readySignal,
            CountDownLatch startSignal,
            CountDownLatch doneSignal
    ) throws Exception {
        readySignal.countDown();
        startSignal.await();
        try {
            return task.call();
        } finally {
            doneSignal.countDown();
        }
    }
}
