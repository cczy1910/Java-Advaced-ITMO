package ru.ifmo.rain.zhukov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Implementation of {@link Crawler} interface using Concurrency Utilities.
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final ConcurrentMap<String, Semaphore> hostSemaphores = new ConcurrentHashMap<>();

    /**
     * Creates WebCrawler with given {@link Downloader} and required limits of downloads, extractors and connections per host .
     *
     * @param downloader  Documents downloader.
     * @param downloaders maximal number of downloading threads.
     * @param extractors  maximal number of extracting threads.
     * @param perHost     maximal number of connections per host threads.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result download(String url, int depth) {
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        Phaser phaser = new Phaser(2);
        Set<String> visited = ConcurrentHashMap.newKeySet();
        visited.add(url);
        downloaders.submit(new DownloadJob(url, depth, downloaded, errors, phaser, visited));
        phaser.arriveAndAwaitAdvance();
        return new Result(new ArrayList<>(downloaded), errors);
    }

    private abstract static class CrawlJob implements Runnable {
        protected final int depth;
        protected final Set<String> downloaded;
        protected final ConcurrentMap<String, IOException> errors;
        protected final Phaser phaser;
        protected final Set<String> visited;

        CrawlJob(int depth, Set<String> downloaded, ConcurrentMap<String, IOException> errors, Phaser phaser, Set<String> visited) {
            this.depth = depth;
            this.downloaded = downloaded;
            this.errors = errors;
            this.phaser = phaser;
            this.visited = visited;
        }
    }

    private class DownloadJob extends CrawlJob {
        private final String url;

        DownloadJob(String url, int depth, Set<String> downloaded, ConcurrentMap<String, IOException> errors, Phaser phaser, Set<String> visited) {
            super(depth, downloaded, errors, phaser, visited);
            this.url = url;
        }

        @Override
        public void run() {
            try {
                String host = URLUtils.getHost(url);
                Semaphore hostSemaphore = hostSemaphores.computeIfAbsent(host, h -> new Semaphore(perHost));
                try {
                    hostSemaphore.acquireUninterruptibly();
                    Document document = downloader.download(url);
                    downloaded.add(url);
                    if (depth > 1) {
                        phaser.register();
                        extractors.submit(new ExtractJob(document, depth - 1, downloaded, errors, phaser, visited));
                    }
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    hostSemaphore.release();
                }
            } catch (MalformedURLException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
            }
        }
    }

    private class ExtractJob extends CrawlJob {
        private final Document document;

        ExtractJob(Document document, int depth, Set<String> downloaded, ConcurrentMap<String, IOException> errors, Phaser phaser, Set<String> visited) {
            super(depth, downloaded, errors, phaser, visited);
            this.document = document;
        }

        @Override
        public void run() {
            try {
                for (String url : document.extractLinks()) {
                    if (visited.add(url)) {
                        phaser.register();
                        downloaders.submit(new DownloadJob(url, depth, downloaded, errors, phaser, visited));
                    }
                }
            } catch (IOException ignored) {
            } finally {
                phaser.arrive();
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    /**
     * Main function to provide console interface of the program.
     * Allowed signature: {@code url [depth [downloads [extractors [perHost]]]]}
     * All arguments must not be null.
     *
     * @param args Provided to program arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4) {
            System.out.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }
        int[] limits = new int[]{1, 1, 1, 1};
        try {
            for (int i = 1; i < args.length; i++) {
                limits[i - 1] = Integer.parseInt(args[i]);
            }
        } catch (NullPointerException e) {
            System.out.println("Null argument!");
            return;
        } catch (NumberFormatException e) {
            System.out.println("Wrong number format!");
            return;
        }
        WebCrawler crawler;
        try {
            crawler = new WebCrawler(new CachingDownloader(), limits[1], limits[2], limits[3]);
            crawler.download(args[0], limits[0]);
        } catch (IOException e) {
            //errs in stdout
            System.out.println("Wrong number format!");
        }
    }
}
