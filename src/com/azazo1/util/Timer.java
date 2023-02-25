package com.azazo1.util;

import java.util.Date;
import java.util.Vector;
/**
 * java 的 Timer 不精确, 我又不想改代码, 就把它搬过来自己写了个循环调用
 * */
public class Timer {
    public Timer(String threadName) {
        executingThread.setName(threadName);
        executingThread.setDaemon(true);
        executingThread.start();
    }

    public void cancel() {
        executingThread.interrupt();
        synchronized (tasks) {
            for (TimerTask task : tasks) {
                synchronized (task.lock) {
                    task.state = TimerTask.CANCELLED;
                }
            }
            tasks.clear();
        }
    }

    /**
     * @see java.util.Timer#schedule(java.util.TimerTask, long, long)
     */
    public void schedule(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, System.currentTimeMillis() + delay, -period);
    }

    public abstract static class TimerTask implements Runnable {
        /**
         * This object is used to control access to the TimerTask internals.
         */
        final Object lock = new Object();

        /**
         * The state of this task, chosen from the constants below.
         */
        int state = VIRGIN;

        /**
         * This task has not yet been scheduled.
         */
        static final int VIRGIN = 0;

        /**
         * This task is scheduled for execution.  If it is a non-repeating task,
         * it has not yet been executed.
         */
        static final int SCHEDULED = 1;

        /**
         * This non-repeating task has already executed (or is currently
         * executing) and has not been cancelled.
         */
        static final int EXECUTED = 2;

        /**
         * This task has been cancelled (with a call to TimerTask.cancel).
         */
        static final int CANCELLED = 3;

        /**
         * Next execution time for this task in the format returned by
         * System.currentTimeMillis, assuming this task is scheduled for execution.
         * For repeating tasks, this field is updated prior to each task execution.
         */
        long nextExecutionTime;

        /**
         * Period in milliseconds for repeating tasks.  A positive value indicates
         * fixed-rate execution.  A negative value indicates fixed-delay execution.
         * A value of 0 indicates a non-repeating task.
         */
        long period = 0;

        /**
         * Creates a new timer task.
         */
        protected TimerTask() {
        }

        /**
         * The action to be performed by this timer task.
         */
        public abstract void run();

        /**
         * Cancels this timer task.  If the task has been scheduled for one-time
         * execution and has not yet run, or has not yet been scheduled, it will
         * never run.  If the task has been scheduled for repeated execution, it
         * will never run again.  (If the task is running when this call occurs,
         * the task will run to completion, but will never run again.)
         *
         * <p>Note that calling this method from within the {@code run} method of
         * a repeating timer task absolutely guarantees that the timer task will
         * not run again.
         *
         * <p>This method may be called repeatedly; the second and subsequent
         * calls have no effect.
         *
         * @return true if this task is scheduled for one-time execution and has
         * not yet run, or this task is scheduled for repeated execution.
         * Returns false if the task was scheduled for one-time execution
         * and has already run, or if the task was never scheduled, or if
         * the task was already cancelled.  (Loosely speaking, this method
         * returns {@code true} if it prevents one or more scheduled
         * executions from taking place.)
         */
        public boolean cancel() {
            synchronized (lock) {
                boolean result = (state == SCHEDULED);
                state = CANCELLED;
                return result;
            }
        }

        /**
         * Returns the <i>scheduled</i> execution time of the most recent
         * <i>actual</i> execution of this task.  (If this method is invoked
         * while task execution is in progress, the return value is the scheduled
         * execution time of the ongoing task execution.)
         *
         * <p>This method is typically invoked from within a task's run method, to
         * determine whether the current execution of the task is sufficiently
         * timely to warrant performing the scheduled activity:
         * <pre>{@code
         *   public void run() {
         *       if (System.currentTimeMillis() - scheduledExecutionTime() >=
         *           MAX_TARDINESS)
         *               return;  // Too late; skip this execution.
         *       // Perform the task
         *   }
         * }</pre>
         * This method is typically <i>not</i> used in conjunction with
         * <i>fixed-delay execution</i> repeating tasks, as their scheduled
         * execution times are allowed to drift over time, and so are not terribly
         * significant.
         *
         * @return the time at which the most recent execution of this task was
         * scheduled to occur, in the format returned by Date.getTime().
         * The return value is undefined if the task has yet to commence
         * its first execution.
         * @see Date#getTime()
         */
        public long scheduledExecutionTime() {
            synchronized (lock) {
                return (period < 0 ? nextExecutionTime + period
                        : nextExecutionTime - period);
            }
        }
    }

    protected final Vector<TimerTask> tasks = new Vector<>();
    protected final Thread executingThread = new Thread() {
        @Override
        public void run() {
            while (!isInterrupted()) {
                Vector<TimerTask> tobeRemoved = new Vector<>();
                synchronized (tasks) {
                    long currentTime, executionTime;
                    boolean taskFired;
                    for (TimerTask task : tasks) {
                        synchronized (task.lock) {
                            if (task.state == TimerTask.CANCELLED) {
                                tobeRemoved.add(task);
                                continue;  // No action required, poll queue again
                            }
                            currentTime = System.currentTimeMillis();
                            executionTime = task.nextExecutionTime;
                            taskFired = (executionTime <= currentTime);
                            if (taskFired) {
                                if (task.period == 0) { // Non-repeating, remove
                                    tobeRemoved.add(task);
                                    task.state = TimerTask.EXECUTED;
                                } else { // Repeating task, reschedule
                                    task.nextExecutionTime =
                                            task.period < 0 ? currentTime - task.period
                                                    : executionTime + task.period;
                                }
                            }
                        }
                        if (taskFired) {
                            task.run();
                        }
                    }
                }
                for (TimerTask timerTask : tobeRemoved) {
                    tasks.remove(timerTask);
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };


    /**
     * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long, long)
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        sched(task, System.currentTimeMillis() + delay, period);
    }

    private void sched(TimerTask task, long time, long period) {
        if (time < 0)
            throw new IllegalArgumentException("Illegal execution time.");

        // Constrain value of period sufficiently to prevent numeric
        // overflow while still being effectively infinitely large.
        if (Math.abs(period) > (Long.MAX_VALUE >> 1))
            period >>= 1;

        synchronized (tasks) {
            synchronized (task.lock) {
                if (task.state != TimerTask.VIRGIN)
                    throw new IllegalStateException(
                            "Task already scheduled or cancelled");
                task.nextExecutionTime = time;
                task.period = period;
                task.state = TimerTask.SCHEDULED;
            }

            tasks.add(task);
        }
    }
}
