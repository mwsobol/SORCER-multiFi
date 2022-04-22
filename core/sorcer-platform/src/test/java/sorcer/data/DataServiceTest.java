/*
 * Distribution Statement
 *
 * This computer software has been developed under sponsorship of the United States Air Force Research Lab. Any further
 * distribution or use by anyone or any data contained therein, unless otherwise specifically provided for,
 * is prohibited without the written approval of AFRL/RQVC-MSTC, 2210 8th Street Bldg 146, Room 218, WPAFB, OH  45433
 *
 * Disclaimer
 *
 * This material was prepared as an account of work sponsored by an agency of the United States Government. Neither
 * the United States Government nor the United States Air Force, nor any of their employees, makes any warranty,
 * express or implied, or assumes any legal liability or responsibility for the accuracy, completeness, or usefulness
 * of any information, apparatus, product, or process disclosed, or represents that its use would not infringe privately
 * owned rights.
 */
package sorcer.data;

import org.junit.Before;
import org.junit.Test;
import org.rioproject.impl.util.FileUtils;
import org.rioproject.security.SecureEnv;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sorcer.data.DataService.DATA_DIR;
import static sorcer.data.DataService.DATA_PORT;

public class DataServiceTest {
    @Before
    public void reset() throws Exception {
        System.clearProperty(DATA_DIR);
        System.clearProperty(DATA_PORT);
        SecureEnv.setup();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNoRoots() {
        new DataService();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithBogusRoot() {
        new DataService("flippity-gibbit");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotNavigable() throws IOException {
        DataService dataService = new DataService(System.getProperty("user.dir"));
        try {
            dataService.start();
            dataService.getDataURL(new File(System.getProperty("user.home")));
        } finally {
            dataService.stop();
        }
    }

    @Test
    public void testStartAndServe() throws IOException {
        DataService dataService = new DataService(System.getProperty("user.dir"));
        try {
            dataService.start();
            assertTrue(verify(dataService.getDataURL(new File(System.getProperty("user.dir")))));
        } finally {
            dataService.stop();
        }
    }

    @Test
    public void testStartUsingPortProperty() {
        System.setProperty(DATA_PORT, "8180");
        DataService dataService = new DataService(System.getProperty("user.dir"));
        try {
            dataService.start();
            String dataUrl = dataService.getDataUrl();
            assertTrue(dataUrl.contains("8180"));
        } finally {
            dataService.stop();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testStartAndStop() throws IOException {
        DataService dataService = new DataService(System.getProperty("user.dir"));
        dataService.start();
        assertTrue(verify(dataService.getDataURL(new File(System.getProperty("user.dir")))));
        dataService.stop();
        dataService.getDataURL(System.getProperty("user.dir"));
    }

    @Test
    public void testMultiple() throws IOException {
        DataService dataService = new DataService(20001, System.getProperty("user.dir")).start();
        DataService dataService2 = new DataService(20001, System.getProperty("user.dir")).start();
        assertTrue(verify(dataService.getDataURL(new File(System.getProperty("user.dir")))));
        assertTrue(verify(dataService2.getDataURL(new File(System.getProperty("user.dir")))));
        dataService.stop();
        dataService2.getDataURL(System.getProperty("user.dir"));
    }

    @Test
    public void testMultiple2() throws IOException {
        DataService dataService = new DataService(20001, System.getProperty("user.dir")).start();
        DataService dataService2 = DataService.getPlatformDataService();
        assertTrue(verify(dataService.getDataURL(new File(System.getProperty("user.dir")))));
        assertTrue(verify(dataService2.getDataURL(DataService.getDataDir())));
        dataService.stop();
        dataService2.getDataURL(DataService.getDataDir());
    }

    @Test
    public void testPlatformDataService() {
        System.clearProperty(DataService.DATA_URL);
        File dataDir = new File(DataService.getDataDir());
        if(!dataDir.exists())
            dataDir.mkdirs();
        DataService dataService = DataService.getPlatformDataService();
        dataService.start();
    }

    @Test
    public void testGetFileFromURL() throws IOException {
        File root = new File(System.getProperty("java.io.tmpdir"), "mstc-eng-test");
        root.mkdirs();
        DataService dataService = new DataService(root.getPath());
        dataService.start();
        File f1 = new File(root, "foo.bar");
        f1.createNewFile();
        f1.deleteOnExit();
        System.out.println("Created " + f1.getPath());
        URL url = dataService.getDataURL(f1);
        File f2 = dataService.getDataFile(url);
        assertTrue(f2.getPath().equals(f1.getPath()));
        File f3 = new File(root, "foo.baz");
        f3.createNewFile();
        f3.deleteOnExit();
        dataService.download(url, f3);
    }

    @Test
    public void testDownloadFromURL() throws IOException {
        File root = new File(System.getProperty("java.io.tmpdir"), "mstc-eng-test");
        root.mkdirs();
        DataService dataService = new DataService(root.getPath());
        dataService.start();
        File f1 = new File(root, "foo.bar");
        Files.write(f1.toPath(), "POTATO".getBytes());
        f1.deleteOnExit();
        System.out.println("Created " + f1.getPath());
        URL url = dataService.getDataURL(f1);
        File f2 = new File(root, "foo.baz");
        f2.deleteOnExit();
        dataService.download(url, f2);
        String orig = new String(Files.readAllBytes(f1.toPath()));
        String copy = new String(Files.readAllBytes(f2.toPath()));
        assertEquals(orig, copy);
    }

    @Test(expected=FileNotFoundException.class)
    public void testGetFileFromURLExpectedFailure() throws IOException {
        File root = new File(System.getProperty("java.io.tmpdir"), "mstc-eng-test");
        root.mkdirs();
        DataService dataService = new DataService(root.getPath());
        dataService.start();
        dataService.getDataFile(new URL("http://localhost/bogus"));
    }

    @Test
    public void testGetFileFromURLUsingFile() throws IOException {
        File root = new File(System.getProperty("java.io.tmpdir"), "mstc-eng-test");
        root.mkdirs();
        DataService dataService = new DataService(root.getPath());
        dataService.start();
        File f1 = new File(root, "foo2.bar");
        f1.createNewFile();
        f1.deleteOnExit();
        File f2 = dataService.getDataFile(f1.toURI().toURL());
        assertTrue(f2.getPath().equals(f1.getPath()));
    }

    @Test(expected=FileNotFoundException.class)
    public void testGetFileFromURLUsingFileExpectedFailure() throws IOException {
        File root = new File(System.getProperty("java.io.tmpdir"), "mstc-eng-test");
        root.mkdirs();
        DataService dataService = new DataService(root.getPath());
        dataService.start();
        dataService.getDataFile(new File(System.getProperty("java.io.tmpdir"), "bogus").toURI().toURL());
    }

    @Test
    public void beatingOfTheWeek() throws IOException, ExecutionException, InterruptedException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"),
                                Long.toString(System.currentTimeMillis()));
        try {
            tempDir.mkdirs();
            DataService dataService = new DataService(0, tempDir.getPath()).start();
            List<URL> dataURLs = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                File dir = new File(tempDir, Integer.toString(i));
                dir.mkdirs();
                File data = new File(dir, "data-"+i);
                data.createNewFile();
                String content = write(data);
                System.out.println("Wrote " + data.getPath() + ": " + content);
                dataURLs.add(dataService.getDataURL(data));
            }
            System.out.println("Created " + dataURLs.size() + " data URLs");
            List<Future<Boolean>> futures = new ArrayList<>();
            for (URL dataURL : dataURLs) {
                Callable<Boolean> urlVerifier = new URLVerifier(dataURL);
                FutureTask<Boolean> task = new FutureTask<>(urlVerifier);
                futures.add(task);
                new Thread(task).start();
            }
            for (Future<Boolean> future : futures) {
                assertTrue(future.get());
            }
        } finally {
            /*if (FileUtils.remove(tempDir)) {
                System.out.println("Removed " + tempDir.getPath());
            }*/
        }
    }

    @Test
    public void testGetDataDir() {
        String tmpDir = System.getenv("TMPDIR")==null?System.getProperty("java.io.tmpdir"):System.getenv("TMPDIR");
        String dataDirName = new File(String.format("%s%ssorcer-%s%sdata",
                                                                  tmpDir,
                                                                  File.separator,
                                                                  System.getProperty("user.name"),
                                                                  File.separator)).getAbsolutePath();
        System.err.println("===> "+dataDirName);
        assertEquals(dataDirName, DataService.getDataDir());
    }

    String write(File f) throws IOException {
        String content = String.format("Hello world /%s/%s", f.getParentFile().getName(), f.getName());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(content);
        }
        return content;
    }

    boolean verify(URL url) {
        boolean verified = false;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            //assertTrue(HttpURLConnection.HTTP_OK == connection.getResponseCode());
            verified = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert connection != null;
            connection.disconnect();
        }
        return verified;
    }

    String getContent(URL url) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }
        return content.toString();
    }

    class URLVerifier implements Callable<Boolean> {
        URL dataURL;

        URLVerifier(URL dataURL) {
            this.dataURL = dataURL;
        }

        public Boolean call() throws Exception {
            verify(dataURL);
            String content = getContent(dataURL);
            System.out.println("Verify " + dataURL.toExternalForm()+", file: ["+dataURL.getFile()+"], : ["+content+"]");
            return content.endsWith(dataURL.getFile());
        }
    }
}