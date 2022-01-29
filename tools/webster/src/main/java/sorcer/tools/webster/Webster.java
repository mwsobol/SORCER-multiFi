/*
 * Copyright 2008 the original author or authors.
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.tools.webster;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import org.rioproject.net.HostUtil;
import org.rioproject.web.WebsterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.file.NotDirectoryException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Webster is a HTTP server which can serve code from multiple codebases.
 * Environment variables used to control Webster are as follows:
 * <p/>
 * <table BORDER COLS=3 WIDTH="100%" >
 * <tr>
 * <td>sorcer.tools.webster.port</td>
 * <td>Sets the port for webster to use</td>
 * <td>0</td>
 * </tr>
 * <td>sorcer.tools.webster.root</td>
 * <td>Root directory to serve code from. Webster supports multiple root
 * directories which are separated by a <code>;</code></td>
 * <td>System.getProperty(user.home)</td>
 * </tr>
 * <p/>
 * </table>
 *
 * @author Dennis Reedy and Mike Sobolewski
 */
public class Webster implements WebsterService, Runnable {
    static final String BASE_COMPONENT = "sorcer.tools";
    public static final String CODESERVER = BASE_COMPONENT + ".codeserver";

    static final int DEFAULT_MIN_THREADS = 1;
    static final int DEFAULT_MAX_THREADS = 10;
    private ServerSocket ss;
    private int port;
    private volatile boolean run = true;
    private static final Properties MimeTypes = new Properties();
    private String[] websterRoot;
    private ThreadPoolExecutor pool;
    private int minThreads = DEFAULT_MIN_THREADS;
    private int maxThreads = DEFAULT_MAX_THREADS;
    private int startPort = 0;
    private int endPort = 0;
    private int soTimeout = 0;
    private static final Logger logger = LoggerFactory.getLogger(Webster.class.getName());
    private com.sun.jini.start.LifeCycle lifeCycle;
    private boolean debug = false;
    private boolean isDaemon = false;
    private static final String SERVER_DESCRIPTION = Webster.class.getName();
    private String tempDir;
    // Shared class server (webster) 
    private static Webster webster;
    private InetAddress addr;
    private boolean started;
    private final List<String> expandedRoots = new LinkedList<>();

    /**
     * Create a new Webster. The port is determined by the
     * webster.port system property. If the
     * webster.port system property does not exist, an
     * anonymous port will be allocated.
     *
     * @throws Exception if Webster cannot create a socket
     */
    public Webster() throws Exception {
        String s = System.getProperty("webster.port");
        if (s != null && !s.equals("0")) {
            try {
                port = new Integer(s);
            } catch (NumberFormatException e) {
                if (s.equals("${webster.port}")) {
                    throw new RuntimeException("The required system property for 'webster.port' not set");
                }
            }
        } else {
            port = getAvailablePort();
        }
        this.isDaemon = false;
        initialize();
    }

    /**
     * Create a new Webster
     *
     * @param port The port to use
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(int port, boolean isDaemon) throws Exception {
        this.port = port;
        this.isDaemon = isDaemon;
        initialize();
    }

    /**
     * Create a new Webster
     *
     * @param roots The root(s) to serve code from. This is a semi-colin
     *              delimited list of directories
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(String roots, boolean isDaemon) throws Exception {
        String s = System.getProperty("webster.port");
        if (s != null) {
            port = new Integer(s);
        } else {
            port = getAvailablePort();
        }
        this.isDaemon = isDaemon;
        initialize(roots);
    }

    /**
     * Create a new Webster
     *
     * @param port  The port to use
     * @param roots The root(s) to serve code from. This is a semi-colin
     *              delimited list of directories
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(int port, String roots, boolean isDaemon) throws Exception {
        this.port = port;
        this.isDaemon = isDaemon;
        initialize(roots);
    }

    /**
     * Create a new Webster
     *
     * @param port  The port to use
     * @param roots The root(s) to serve code from. This is a semi-colin
     *              delimited list of directories
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(int port, String roots, String tempDir) throws Exception {
        this.port = port;
        this.tempDir  = tempDir;
        isDaemon = true;
        initialize(roots);
    }

    /**
     * Create a new Webster
     *
     * @param port        The port to use
     * @param roots       The root(s) to serve code from. This is a semi-colin
     *                    delimited list of directories
     * @param bindAddress TCP/IP address which Webster should bind to (null
     *                    implies no specific address)
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(int port, String roots, String bindAddress, boolean isDaemon) throws Exception {
        this.port = port;
        this.isDaemon = isDaemon;
        initialize(roots, bindAddress);
    }

    public Webster(int port, String[] roots, String bindAddress,boolean isDaemon) throws Exception {
        this.port = port;
        this.isDaemon = isDaemon;
        initialize(roots, bindAddress);
    }

    /**
     * Create a new Webster
     *
     * @param port        The port to use
     * @param roots       The root(s) to serve code from. This is a semi-colin
     *                    delimited list of directories
     * @param bindAddress TCP/IP address which Webster should bind to (null
     *                    implies no specific address)
     * @param minThreads  Minimum threads to use in the ThreadPool
     * @param maxThreads  Minimum threads to use in the ThreadPool
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(int port,
                   String roots,
                   String bindAddress,
                   int minThreads,
                   int maxThreads,
                   boolean isDaemon) throws Exception {
        this.port = port;
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.isDaemon = isDaemon;
        initialize(roots, bindAddress);
    }

    /**
     * Create a new Webster
     *
     * @param port        The port to use
     * @param roots       The root(s) to serve code from. This is a semi-colin
     *                    delimited list of directories
     * @param bindAddress TCP/IP address which Webster should bind to (null
     *                    implies no specific address)
     * @param minThreads  Minimum threads to use in the ThreadPool
     * @param maxThreads  Minimum threads to use in the ThreadPool
     * @param startPort   First port to try to listen
     * @param endPort     Last port to try to listen
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(int port,
                   String roots,
                   String bindAddress,
                   int minThreads,
                   int maxThreads,
                   int startPort,
                   int endPort,
                   boolean isDaemon) throws Exception {
        this.port = port;
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.startPort = startPort;
        this.endPort = endPort;
        this.isDaemon = isDaemon;
        initialize(roots, bindAddress);
    }

    /**
     * Create a new Webster, compatible with the ServiceStarter mechanism in
     * Jini 2.0
     *
     * @param args      String[] of options. Valid options are [-port port],
     *                  [-roots list-of-roots], [-bindAddress address], [-minThreads minThreads],
     *                  [-maxThreads maxThreads] [-soTimeout soTimeout]
     * @param lifeCycle The LifeCycle object, may be null
     * @throws Exception if Webster cannot create a socket
     */
    public Webster(String[] args, com.sun.jini.start.LifeCycle lifeCycle)
            throws Exception {
        if (args == null)
            throw new NullPointerException("Configuration is null");

        Webster.webster = this;
        this.lifeCycle = lifeCycle;
        String[] configRoots = null;
        String[] configArgs = null;
        String roots = null;
        String[] options;
        String bindAddress = null;
        if (args.length == 1 && new File(args[0]).isFile()) {
            final Configuration config = ConfigurationProvider.getInstance(args);
            try {
                configRoots = (String[]) config.getEntry(CODESERVER,
                                                         "roots", String[].class);
            } catch (Exception e) {
                e.printStackTrace();
                configRoots = null;
            }
            try {
                configArgs = (String[]) config.getEntry(CODESERVER,
                                                        "options", String[].class);
            } catch (Exception e) {
                e.printStackTrace();
                configArgs = null;
            }
//			logger.info("webster roots: " + Arrays.toString(configRoots));
//			logger.info("webster options: " + Arrays.toString(configArgs));
        }
        if (configRoots != null) {
            websterRoot = configRoots;
            options = configArgs;
        } else {
            options = args;
        }
//        logger.info("roots concat: " + roots + "\noptions: " + Arrays.toString(options));
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            if (option.equals("-port")) {
                i++;
                this.port = Integer.parseInt(options[i]);
            } else if (option.equals("-roots")) {
                i++;
                roots = options[i];
            } else if (option.equals("-bindAddress")) {
                i++;
                bindAddress = options[i];
            } else if (option.equals("-startPort")) {
                i++;
                startPort = Integer.parseInt(options[i]);
            } else if (option.equals("-endPort")) {
                i++;
                endPort = Integer.parseInt(options[i]);
            } else if (option.equals("-minThreads")) {
                i++;
                minThreads = Integer.parseInt(options[i]);
            } else if (option.equals("-maxThreads")) {
                i++;
                maxThreads = Integer.parseInt(options[i]);
            } else if (option.equals("-soTimeout")) {
                i++;
                soTimeout = Integer.parseInt(options[i]);
            } else if (option.equals("-isDaemon")) {
                i++;
                isDaemon = Boolean.parseBoolean(options[i]);
            } else if (option.equals("-debug")) {
                i++;
                debug = Boolean.parseBoolean(options[i]);
            } else {
                throw new IllegalArgumentException(option);
            }
        }
        if (websterRoot != null)
            init(bindAddress);
        else
            initialize(roots, bindAddress);

    }

    /*
     * Initialize Webster, serving code as determined by the either the
     * sorcer.tools.webster.root system property (if set) or defaulting to
     * the user.dir system property
     */
    private void initialize() throws Exception {
        String root = System.getProperty("webster.root");
        if (root == null)
            root = System.getProperty("user.dir");
        initialize(root);
    }

    /*
     * Initialize Webster
     * 
     * @param roots The root(s) to serve code from. This is a semicolon
     * delimited list of directories
     */
    private void initialize(String roots) throws Exception {
        initialize(roots, null);
    }

    private void initialize(String roots, String bindAddress) throws Exception {
        setupRoots(roots);
        init(bindAddress);
    }

    private void initialize(String[] roots, String bindAddress) throws Exception {
        websterRoot = roots;
        init(bindAddress);
    }

    /*
     * Initialize Webster
     *
     * @param roots The root(s) to serve code from. This is a semicolon
     * delimited list of directories
     */
    private void init(String bindAddress) throws Exception {
        String str;
        if (!debug) {
            str = System.getProperty("webster.debug");
            if (str != null && str.equals("true"))
                debug = true;
        }
        str = System.getProperty("webster.tmp.dir");
        if (str != null) {
            tempDir = str;
        }
        if(tempDir!=null)
            logger.debug("tempDir: " + tempDir);

        for (int j = 0; j < websterRoot.length; j++) {
            if (debug) {
                System.out.println("Root " + j + " = " + websterRoot[j]);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Root " + j + " = " + websterRoot[j]);
            }
        }


        try {
            if (bindAddress == null) {
                bindAddress = System.getProperty("webster.interface");
            }
            if (bindAddress == null) {
                bindAddress = HostUtil.getInetAddressFromProperty("java.rmi.server.hostname").getHostAddress();
            }
            addr = InetAddress.getByName(bindAddress);
        } catch (UnknownHostException e) {
            logger.error("Bind address server socket failure", e);
            return;
        }

        start();
    }

    /*private void start() throws IOException {
        if (port == 0) {
            port = getPortAvailable();
            startPort = port;
            endPort = port;
        } else {
            if (startPort == endPort) {
                startPort = port;
                endPort = port;
            }
        }

        for (int i = startPort; i <= endPort; i++) {
            start(i, addr);
            return;
        }
    }*/

    // start with the first available port in the range STARTPORT-ENDPORT
    private void start(int websterPort, InetAddress address) throws IOException {
        try {
            port = websterPort;
            // check if the port is not required by the JVM system property
            String s = System.getProperty("webster.port");
            if (s != null && s.length() > 0) {
                port = new Integer(s);
            }
            ss = new ServerSocket(port, 0, address);
        } catch (IOException ioe) {
            if (startPort == endPort) {
                throw new IOException("Port bind server socket failure: " + endPort, ioe);
            } else {
                logger.error("Port bind server socket failure: " + port);
                throw ioe;
            }
        }
        port = ss.getLocalPort();

        if (debug)
            System.out.println("Webster serving on: "
                                       + ss.getInetAddress().getHostAddress() + ":" + port);
        if (logger.isDebugEnabled())
            logger.info("Webster serving on: "
                                + ss.getInetAddress().getHostAddress() + ":" + port);
        if (debug)
            System.out.println("Webster listening on port: " + port);
        if (logger.isDebugEnabled()) {
            logger.debug("Webster listening on port: " + port);
        }
        try {
            pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);
            if (debug)
                System.out.println("Webster minThreads [" + minThreads + "], "
                                           + "maxThreads [" + maxThreads + "]");
            if (logger.isDebugEnabled())
                logger.debug("Webster minThreads [" + minThreads + "], "
                                    + "maxThreads [" + maxThreads + "]");
        } catch (Exception e) {
            logger.error("Could not create ThreadPool", e);
            throw new RuntimeException("Could not create Thread Pool");
        }
        if (soTimeout > 0) {
            if (debug)
                System.out.println("Webster Socket SO_TIMEOUT set to ["
                                           + soTimeout + "] millis");
            if (logger.isDebugEnabled())
                logger.debug("Webster Socket SO_TIMEOUT set to [" + soTimeout
                                    + "] millis");
        }
        /* Set system property */
        System.setProperty(CODESERVER, "http://" + getAddress() + ":"+ getPort());

        if (logger.isDebugEnabled())
            logger.debug("Webster isDaemon: " + isDaemon);

        Thread runner = new Thread(this, "Webster");
        if (isDaemon) {
            runner.setDaemon(true);
        }
        runner.start();
    }

    /**
     * Get the roots Webster is serving
     *
     * @return The roots Webster is serving as a semicolon delimited String
     */
    public String getRoots() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < websterRoot.length; i++) {
            if (i > 0)
                buffer.append(";");
            buffer.append(websterRoot[i]);
        }
        return (buffer.toString());
    }

    /**
     * Get address that Webster is bound to
     *
     * @return The host address the server socket Webster is using is bound to.
     *         If the socket is null, return null.
     */
    public String getAddress() {
        if (ss == null)
            return (null);
        return (ss.getInetAddress().getHostAddress());
    }

    /*
     * Setup the websterRoot property
     */
    private void setupRoots(String roots) {
        if (roots == null)
            throw new NullPointerException("roots is null");
        StringTokenizer tok = new StringTokenizer(roots, ";");
        websterRoot = new String[tok.countTokens()];
        if (websterRoot.length > 1) {
            for (int j = 0; j < websterRoot.length; j++) {
                websterRoot[j] = tok.nextToken();
                if (debug)
                    System.out.println("Root " + j + " = " + websterRoot[j]);
                if (logger.isDebugEnabled())
                    logger.debug("Root " + j + " = " + websterRoot[j]);
            }
        } else {
            websterRoot[0] = roots;
            if (debug)
                System.out.println("Root  = " + websterRoot[0]);
            if (logger.isDebugEnabled())
                logger.debug("Root  = " + websterRoot[0]);
        }
    }

    @Override
    public WebsterService setRoots(String... roots) {
        return this;
    }

    @Override
    public URI getURI() {
        try {
            return new URI(String.format("http://%s:%s", getAddress(), getPort()));
        } catch (URISyntaxException e) {
            logger.error("Failed getting URI", e);
        }
        return null;
    }

    @Override
    public void start() throws Exception {
        if (started)
            return;
        if (port == 0) {
            port = getPortAvailable();
            startPort = port;
            endPort = port;
        } else {
            if (startPort == endPort) {
                startPort = port;
                endPort = port;
            }
        }

        for (int i = startPort; i <= endPort; i++) {
            start(i, addr);
            started = true;
            return;
        }
    }

    @Override
    public void startSecure() throws Exception {
        throw new Exception("https is not implemented");
    }

    /**
     * Terminate a running Webster instance
     */
    public void terminate() {
        run = false;
        if (ss != null) {
            try {
                ss.close();
            } catch (IOException e) {
                logger.warn("Exception closing Webster ServerSocket");
            }
        }
        if (lifeCycle != null)
            lifeCycle.unregister(this);

        if (pool != null)
            pool.shutdownNow();
    }

    /**
     * Get the port Webster is bound to
     *
     * @return The port Webster is bound to
     */
    public int getPort() {
        return getAvailablePort();
    }

    private String readRequest(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int read;
        int prev = -1;
        while ((read = inputStream.read()) != -1) {
            if (read != '\n' && read != '\r')
                sb.append((char) read);
            if (read == '\n' && prev == '\r') {
                break;
            }
            if (read == '\r' && prev == '0') {
                //sb.delete(0, sb.length());
                break;
            }
            prev = read;
        }
        return sb.toString();
    }


    public void run() {
        Socket s;
        try {
            loadMimes();
            String fileName;
            while (run) {
                s = ss.accept(); // accept incoming requests
                if (soTimeout > 0) {
                    s.setSoTimeout(soTimeout);
                }
                String line;
                Properties header = new Properties();
                DataInputStream inputStream = null;
                try {
                    inputStream = new DataInputStream(s.getInputStream());
                    StringBuilder lineBuilder = new StringBuilder();
                    StringTokenizer tokenizer;
                    while ((line = readRequest(inputStream)).length() != 0) {
                        if (lineBuilder.length() > 0)
                            lineBuilder.append("\n");
                        lineBuilder.append(line);
                        tokenizer = new StringTokenizer(line, ":");
                        String aToken = tokenizer.nextToken().trim();
                        if (tokenizer.hasMoreTokens()) {
                            header.setProperty(aToken, tokenizer.nextToken().trim());
                        }
                    }
                    line = lineBuilder.toString();
                    int port = s.getPort();
                    String from = s.getInetAddress().getHostAddress() + ":" + port;
                    if (debug) {
                        StringBuilder buff = new StringBuilder();
                        buff.append("From: ").append(from).append(", ");
                        if (soTimeout > 0)
                            buff.append("SO_TIMEOUT: ").append(soTimeout).append(", ");
                        buff.append("Request: ").append(line);
                        System.out.println("\n"+buff.toString());
                    }
                    if (logger.isDebugEnabled()) {
                        StringBuilder buff = new StringBuilder();
                        buff.append("From: ").append(from).append(", ");
                        if (soTimeout > 0)
                            buff.append("SO_TIMEOUT: ").append(soTimeout).append(", ");
                        buff.append("Request: ").append(line);
                        if (logger.isDebugEnabled())
                            logger.debug(buff.toString());
                    }
                    if (line.length() > 0) {
                        tokenizer = new StringTokenizer(line, " ");
                        if (!tokenizer.hasMoreTokens())
                            break;
                        String token = tokenizer.nextToken();
                        fileName = tokenizer.nextToken();
                        if (fileName.startsWith("/"))
                            fileName = fileName.substring(1);
                        if (token.equals("GET")) {
                            header.setProperty("GET", fileName);
                        } else if (token.equals("PUT")) {
                            header.setProperty("PUT", fileName);
                        } else if (token.equals("DELETE")) {
                            header.setProperty("DELETE", fileName);
                        } else if (token.equals("HEAD")) {
                            header.setProperty("HEAD", fileName);
                        }
                        while (tokenizer.hasMoreTokens()) {
                            String aToken = tokenizer.nextToken().trim();
                            if (tokenizer.hasMoreTokens()) {
                                header.setProperty(aToken,
                                                   tokenizer.nextToken().trim());
                            }
                        }
                        if (header.getProperty("GET") != null) {
                            pool.execute(new GetFile(s, fileName));
                        } else if (header.getProperty("PUT") != null) {
                            if(tempDir==null) {
                                DataOutputStream clientStream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                                clientStream.writeBytes("HTTP/1.1 405 Method Not Allowed\nWebster is in read-only mode\r\n\r\n");
                                clientStream.flush();
                                clientStream.close();
                            } else {
                                pool.execute(new PutFile(s, fileName, header, inputStream));
                            }
                        } else if (header.getProperty("DELETE") != null) {
                            pool.execute(new DelFile(s, fileName));
                        } else if (header.getProperty("HEAD") != null) {
                            pool.execute(new Head(s, fileName));
                        } else {
                            if (debug)
                                System.out.println("bad request [" + line + "] from " + from);
                            if (logger.isDebugEnabled())
                                logger.debug("bad request [" + line + "] from " + from);
                            DataOutputStream clientStream =
                                new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                            clientStream.writeBytes("HTTP/1.0 400 Bad Request\r\n\r\n");
                            clientStream.flush();
                            clientStream.close();
                        }
                    } /* if line != null */
                } catch (Exception e) {
                    DataOutputStream clientStream =
                            new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                    clientStream.writeBytes("HTTP/1.0 500 Internal Server Error\n" +
                                            "MIME-Version: 1.0\n" +
                                            "Server: " + SERVER_DESCRIPTION + "\n" +
                                            "\n\n<H1>500 Internal Server Error</H1>\n"
                                            + e);
                    clientStream.flush();
                    clientStream.close();
                    inputStream.close();
                    logger.warn("Getting Request", e);
                }
            }
        } catch (Exception e) {
            if (run) {
                e.printStackTrace();
                logger.warn("Processing HTTP Request", e);
            }
        }
    }

    /**
     * Get an anonymous port
     *
     * @return An available port created by instantiating a
     *         <code>java.net.ServerSocket</code> with a port of 0
     * @throws IOException If an available port cannot be obtained
     */
    public int getPortAvailable() throws java.io.IOException {
        java.net.ServerSocket socket = new java.net.ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    // load the properties file
    void loadMimes() {
        if (debug)
            System.out.println("Loading mimetypes ... ");
        if (logger.isDebugEnabled())
            logger.debug("Loading mimetypes ... ");
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        URL fileURL =
                ccl.getResource("sorcer/tools/webster/mimetypes.properties");
        if (fileURL != null) {
            try {
                InputStream is = fileURL.openStream();
                MimeTypes.load(is);
                is.close();
                if (debug)
                    System.out.println("Mimetypes loaded");
                if (logger.isDebugEnabled())
                    logger.debug("Mimetypes loaded");
            } catch (IOException ioe) {
                logger.error("Loading Mimetypes", ioe);
            }
        } else {
            if (debug)
                System.out.println("mimetypes.properties not found, " +
                                           "loading defaults");
            if (logger.isDebugEnabled())
                logger.debug("mimetypes.properties not found, loading defaults");
            MimeTypes.put("jpg", "image/jpg");
            MimeTypes.put("jpeg", "image/jpg");
            MimeTypes.put("jpe", "image/jpg");
            MimeTypes.put("gif", "image/gif");
            MimeTypes.put("htm", "text/html");
            MimeTypes.put("html", "text/html");
            MimeTypes.put("txt", "text/plain");
            MimeTypes.put("qt", "video/quicktime");
            MimeTypes.put("mov", "video/quicktime");
            MimeTypes.put("class", "application/octet-stream");
            MimeTypes.put("mpg", "video/mpeg");
            MimeTypes.put("mpeg", "video/mpeg");
            MimeTypes.put("mpe", "video/mpeg");
            MimeTypes.put("au", "audio/basic");
            MimeTypes.put("snd", "audio/basic");
            MimeTypes.put("wav", "audio/x-wave");
            MimeTypes.put("JNLP", "application/x-java-jnlp-file");
            MimeTypes.put("jnlp", "application/x-java-jnlp-file");
            MimeTypes.put("java", "application/java");
            MimeTypes.put("jar", "application/java");
            MimeTypes.put("JAR", "application/java");
        }
    } // end of loadMimes

    protected File parseFileName(String filename) throws IOException {
        StringBuilder fn = new StringBuilder(filename);
        for (int i = 0; i < fn.length(); i++) {
            if (fn.charAt(i) == '/')
                fn.replace(i, i + 1, File.separator);
        }
        File f = null;
        String[] roots = expandRoots();
        for (String root : roots) {

            f = new File(root, fn.toString());
            //System.out.println("root: "+root+", looking for "+f.getPath());
            if (f.exists()) {
                return (f);
            }
        }
        return (f);
    }

    private boolean isAmbiguous(File f) {
        String name = f.getName();
        return name.contains("/.");
    }

    private boolean isGoodRequest(File f) throws IOException {
        String path = f.getCanonicalPath();
        String[] roots = expandRoots();
        for (String root : roots) {
            if (path.startsWith(root)) {
                return true;
            }
        }
        return false;
    }

    protected String[] expandRoots() throws IOException {
        if (expandedRoots.isEmpty()) {
            for (String root : websterRoot) {
                File f = new File(root);
                if (!f.exists()) {
                    throw new FileNotFoundException(f.getPath());
                }
                if (!f.isDirectory()) {
                    throw new NotDirectoryException(f.getPath());
                }
                expandedRoots.add(f.getCanonicalPath());
            }
        }
        return expandedRoots.toArray(new String[0]);
    }

    class Head implements Runnable {
        private final Socket client;
        private final String fileName;

        Head(Socket s, String fileName) {
            client = s;
            this.fileName = fileName;
        }

        public void run() {
            StringBuilder dirData = new StringBuilder();
            StringBuilder logData = new StringBuilder();
            try {
                File getFile = parseFileName(fileName);
                logData.append("Do HEAD: input=").append(fileName).append(", parsed=").append(getFile).append(", ");
                int fileLength;
                String header;
                if (!isGoodRequest(getFile)) {
                    header = "HTTP/1.1 400 Bad Request\r\n\r\n";
                    logData.append("bad request");
                } else if(isAmbiguous(getFile)) {
                    header = "HTTP/1.1 400 Ambiguous segment in URI\r\n\r\n";
                    logData.append("ambiguous segment");
                } else if (getFile.isDirectory()) {
                    logData.append("directory located");
                    String files[] = getFile.list();
                    for (String file : files) {
                        File f = new File(getFile, file);
                        dirData.append(f.toString().substring(
                                getFile.getParent().length()));
                        dirData.append("\t");
                        if (f.isDirectory())
                            dirData.append("d");
                        else
                            dirData.append("f");
                        dirData.append("\t");
                        dirData.append(f.length());
                        dirData.append("\t");
                        dirData.append(f.lastModified());
                        dirData.append("\n");
                    }
                    fileLength = dirData.length();
                    String fileType = MimeTypes.getProperty("txt");
                    if (fileType == null)
                        fileType = "application/java";
                    header = "HTTP/1.0 200 OK\n" +
                            "Allow: GET\nMIME-Version: 1.0\n" +
                            "Server: " + SERVER_DESCRIPTION + "\n" +
                            "Content-Type: " + fileType + "\n" +
                            "Content-Length: " + fileLength + "\r\n\r\n";
                } else if (getFile.exists()) {
                    DataInputStream requestedFile = new DataInputStream(
                            new BufferedInputStream(new FileInputStream(getFile)));
                    fileLength = requestedFile.available();
                    String fileType =
                            fileName.substring(fileName.lastIndexOf(".") + 1);
                    fileType = MimeTypes.getProperty(fileType);
                    logData.append("file size: [").append(fileLength).append("]");
                    header = "HTTP/1.0 200 OK\n"
                            + "Allow: GET\nMIME-Version: 1.0\n"
                            + "Server: " + SERVER_DESCRIPTION + "\n"
                            + "Content-Type: "
                            + fileType
                            + "\n"
                            + "Content-Length: "
                            + fileLength
                            + "\r\n\r\n";
                } else {
                    header = "HTTP/1.1 404 Not Found\r\n\r\n";
                    logData.append("not found");
                }

                if (debug)
                    System.out.println(logData.toString());
                if (logger.isDebugEnabled())
                    logger.debug(logData.toString());

                DataOutputStream clientStream =
                        new DataOutputStream(
                                new BufferedOutputStream(client.getOutputStream()));
                clientStream.writeBytes(header);
                clientStream.flush();
                clientStream.close();
            } catch (Exception e) {
                logger.warn("Error closing Socket", e);
            } finally {
                try {
                    client.close();
                } catch (IOException e2) {
                    logger.warn(
                               "Closing incoming socket",
                               e2);
                }
            }
        } // end of Head
    }

    class GetFile implements Runnable {
        private final Socket client;
        private final String fileName;
        private DataInputStream requestedFile;
        private int fileLength;

        GetFile(Socket s, String fileName) {
            client = s;
            this.fileName = fileName;
        }

        public void run() {
            StringBuilder dirData = new StringBuilder();
            StringBuilder logData = new StringBuilder();
            try {
                File getFile = parseFileName(fileName);
                logData.append("Do GET: input=").append(fileName).append(", parsed=").append(getFile).append(", ");
                String header;
                boolean goodRequest = isGoodRequest(getFile);
                if (!goodRequest) {
                    header = "HTTP/1.1 400 Bad Request\r\n\r\n";
                } else if(isAmbiguous(getFile)) {
                    header = "HTTP/1.1 400 Ambiguous segment in URI\r\n\r\n";
                    logData.append("ambiguous segment");
                    goodRequest = false;
                } else if (getFile.isDirectory()) {
                    logData.append("directory located");
                    String[] files = getFile.list();
                    for (String file : files) {
                        File f = new File(getFile, file);
                        dirData.append(f.toString().substring(
                                getFile.getParent().length()));
                        dirData.append("\t");
                        if (f.isDirectory())
                            dirData.append("d");
                        else
                            dirData.append("f");
                        dirData.append("\t");
                        dirData.append(f.length());
                        dirData.append("\t");
                        dirData.append(f.lastModified());
                        dirData.append("\n");
                    }
                    fileLength = dirData.length();
                    String fileType = MimeTypes.getProperty("txt");
                    if (fileType == null)
                        fileType = "application/java";
                    header = "HTTP/1.0 200 OK\n"
                            + "Allow: GET\nMIME-Version: 1.0\n"
                            + "Server: " + SERVER_DESCRIPTION + "\n"
                            + "Content-Type: "
                            + fileType
                            + "\n"
                            + "Content-Length: "
                            + fileLength
                            + "\r\n\r\n";
                } else if (getFile.exists()) {
                    requestedFile = new DataInputStream( new BufferedInputStream(new FileInputStream(getFile)));
                    fileLength = requestedFile.available();
                    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                    fileType = MimeTypes.getProperty(fileType);
                    header = "HTTP/1.0 200 OK\n"
                            + "Allow: GET\nMIME-Version: 1.0\n"
                            + "Server: " + SERVER_DESCRIPTION + "\n"
                            + "Content-Type: " + fileType + "\n"
                            + "Content-Length: " + fileLength + "\r\n\r\n";
                } else {
                    header = "HTTP/1.0 404 Not Found\r\n\r\n";
                }
                DataOutputStream clientStream = new DataOutputStream( new BufferedOutputStream(client.getOutputStream()));
                clientStream.writeBytes(header);

                if (goodRequest) {
                    if (getFile.isDirectory()) {
                        clientStream.writeBytes(dirData.toString());
                    } else if (getFile.exists()) {
                        byte[] buffer = new byte[fileLength];
                        requestedFile.readFully(buffer);
                        logData.append("file size: [").append(fileLength).append("]");
                        try {
                            clientStream.write(buffer);
                        } catch (Exception e) {
                            String s = String.format("Sending [%s], size[%s], to client at [%s]",
                                                     getFile.getAbsolutePath(),
                                                     fileLength,
                                                     client.getInetAddress().getHostAddress());
                            if (logger.isDebugEnabled())
                                logger.debug(s, e);
                            if (debug) {
                                System.out.println(s);
                                e.printStackTrace();
                            }
                        }
                        requestedFile.close();
                    } else {
                        logData.append("not found");
                    }
                }
                if (debug)
                    System.out.println(logData.toString());
                if (logger.isDebugEnabled())
                    logger.debug(logData.toString());
                clientStream.flush();
                clientStream.close();
            } catch (Exception e) {
                logger.warn("Closing Socket", e);
            } finally {
                try {
                    client.close();
                } catch (IOException e2) {
                    logger.warn(
                               "Closing incoming socket",
                               e2);
                }
            }
        } // end of GetFile
    }

    class PutFile implements Runnable {
        private final Socket client;
        private final String fileName;
        private final Properties rheader;
        private final InputStream inputStream;
        final int BUFFER_SIZE = 4096;

        PutFile(Socket s, String fileName, Properties header, InputStream fromClient) {
            rheader = header;
            client = s;
            this.fileName = fileName;
            this.inputStream = fromClient;
        }

        public void run() {

            String s = ignoreCaseProperty(rheader, "Content-Length");
            if (s == null) {
                try {
                    sendResponse("HTTP/1.0 411 OK\n"
                                         + "Allow: PUT\n"
                                         + "MIME-Version: 1.0\n"
                                         + "Server : SORCER Webster: a Java HTTP Server \n"
                                         + "\n\n <H1>411 Webster refuses to accept the out request for " + fileName + " " +
                                         "without a defined Content-Length.</H1>\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String header;
                OutputStream requestedFileOutputStream = null;
                try {
                    // check to see if the file exists if it does the return code
                    // will be 200 if it doesn't it will be 201
                    File putFile;
                    if (tempDir != null) {
                        putFile = new File(tempDir + File.separator + fileName);
                    } else {
                        putFile = parseFileName(fileName);
                    }
                    if (debug)
                        logger.info("tempDir: " + tempDir + ", fileName: " + fileName + ", putFile: " + putFile.getPath());

                    if (putFile.exists()) {
                        header = "HTTP/1.0 200 OK\n"
                                + "Allow: PUT\n"
                                + "MIME-Version: 1.0\n"
                                + "Server : SORCER Webster: a Java HTTP Server \n"
                                + "\n\n <H1>200 PUT File " + fileName + " updated</H1>\n";
                        if (debug)
                            System.out.println("updated putFile: " + putFile);
                    } else {
                        header = "HTTP/1.0 201 Created\n"
                                + "Allow: PUT\n"
                                + "MIME-Version: 1.0\n"
                                + "Server : SORCER Webster: a Java HTTP Server \n"
                                + "\n\n <H1>201 PUT File " + fileName + " Created</H1>\n";
                        File parentDir = putFile.getParentFile();
                        System.out.println("Parent: " + parentDir.getPath() + ", exists? " + parentDir.exists());
                        if (!parentDir.exists()) {
                            if (parentDir.mkdirs() && debug) {
                                System.out.println("Created " + parentDir.getPath());
                            }
                        }
                        if (debug)
                            System.out.println("Created putFile: " + putFile + ", exists? " + putFile.exists());
                    }

                    int length = Integer.parseInt(ignoreCaseProperty(rheader, "Content-Length"));
                    if (debug)
                        logger.info("Putting " + fileName + " size: " + length + ", header: " + rheader);
                    try {
                        requestedFileOutputStream = new DataOutputStream(new FileOutputStream(putFile));
                        int read;
                        long amountRead = 0;
                        byte[] buffer = new byte[length < BUFFER_SIZE ? length : BUFFER_SIZE];
                        while (amountRead < length) {
                            read = inputStream.read(buffer);
                            requestedFileOutputStream.write(buffer, 0, read);
                            amountRead += read;
                        }
                        requestedFileOutputStream.flush();
                        System.out.println("Wrote: " + putFile.getPath() + " size: " + putFile.length());
                    } catch (IOException e) {
                        e.printStackTrace();
                        header = "HTTP/1.0 500 Internal Server Error\n"
                                + "Allow: PUT\n"
                                + "MIME-Version: 1.0\n"
                                + "Server: " + SERVER_DESCRIPTION + "\n"
                                + "\n\n <H1>500 Internal Server Error</H1>\n"
                                + e;
                    } finally {
                        if (requestedFileOutputStream != null)
                            requestedFileOutputStream.close();
                        sendResponse(header);
                    }

                } catch (Exception e) {
                    logger.warn("Closing Socket", e);
                } finally {
                    try {
                        if (requestedFileOutputStream != null)
                            requestedFileOutputStream.close();
                        client.close();
                    } catch (IOException e2) {
                        logger.warn("Closing incoming socket", e2);
                    }
                }
            }
        }

        void sendResponse(String header) throws IOException {
            DataOutputStream clientStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
            clientStream.writeBytes(header);
            clientStream.flush();
            clientStream.close();
        }

        public String ignoreCaseProperty(Properties props, String field) {
            Enumeration<?> names = props.propertyNames();
            while (names.hasMoreElements()) {
                String propName = (String) names.nextElement();
                if (field.equalsIgnoreCase(propName)) {
                    return (props.getProperty(propName));
                }
            }
            return (null);
        }
    } // end of PutFile

    class DelFile implements Runnable {
        private final Socket client;
        private final String fileName;

        DelFile(Socket s, String fileName) {
            client = s;
            this.fileName = fileName;
        }

        public void run() {
            try {
                File delFile = parseFileName(fileName);
                String header;
                if (!isGoodRequest(delFile)) {
                    header = "HTTP/1.1 400 Bad Request\r\n\r\n";
                } else if(isAmbiguous(delFile)) {
                    header = "HTTP/1.1 400 Ambiguous segment in URI\r\n\r\n";
                } else if (!delFile.exists()) {
                    header = "HTTP/1.0 404 File not found\n"
                            + "Allow: DELETE\n"
                            + "MIME-Version: 1.0\n"
                            + "Server: " + SERVER_DESCRIPTION + "\n"
                            + "\n\n <H1>404 File not Found</H1>\n"
                            + "<BR>";
                } else if (delFile.delete()) {
                    header = "HTTP/1.0 200 OK\n"
                            + "Allow: DELETE\n"
                            + "MIME-Version: 1.0\n"
                            + "Server: " + SERVER_DESCRIPTION + "\n"
                            + "\n\n <H1>200 File succesfully deleted</H1>\n";
                } else {
                    header = "HTTP/1.0 500 Internal Server Error\n"
                            + "Allow: DELETE\n"
                            + "MIME-Version: 1.0\n"
                            + "Server: " + SERVER_DESCRIPTION + "\n"
                            + "\n\n <H1>500 File could not be deleted</H1>\n";
                }
                DataOutputStream clientStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
                clientStream.writeBytes(header);
                clientStream.flush();
                clientStream.close();
            } catch (Exception e) {
                logger.warn("Closing Socket", e);
            } finally {
                try {
                    client.close();
                } catch (IOException e2) {
                    logger.warn("Closing incoming socket", e2);
                }
            }
        }
    }

    public static int getWebsterPort() {
        return new Integer(System.getProperty("webster.port"));
    }

    public int getAvailablePort() {
        if (port == 0) {
            java.net.ServerSocket socket;
            try {
                socket = new java.net.ServerSocket(0);
                port = socket.getLocalPort();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return port;
    }

    public static void main(String[] args) {
        try {
            new Webster();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Returns the embedded class server (webster) for this environment.
     * </p>
     *
     * @return the embedded class server
     */
    public static Webster getWebster() {
        return webster;
    }
}
