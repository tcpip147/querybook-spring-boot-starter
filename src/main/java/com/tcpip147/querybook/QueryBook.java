package com.tcpip147.querybook;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.tcpip147.querybook.QueryBookConfigParams.*;

public class QueryBook {

    private static final Logger LOG = LoggerFactory.getLogger(QueryBook.class);

    private String root;
    private boolean devMode;
    private boolean injectComment;

    private DocumentBuilder xmlParser;
    private Map<String, Query> queryStore = new HashMap<>();

    public QueryBook(QueryBookConfig queryBookConfig) throws ParserConfigurationException {
        root = (String) queryBookConfig.get(ROOT);
        devMode = (boolean) queryBookConfig.get(DEV_MODE);
        injectComment = (boolean) queryBookConfig.get(INJECT_COMMENT);
        xmlParser = DocumentBuilderFactory.newNSInstance().newDocumentBuilder();
    }

    @PostConstruct
    private void init() throws IOException {
        File root = getRootDirectory();
        try (Stream<Path> stream = Files.walk(root.toPath())) {
            stream.filter(path -> {
                File file = new File(path.toUri());
                return file.isFile();
            }).forEach(path -> {
                try {
                    storeQueryFromFile(path, true);
                } catch (IOException | SAXException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        if (devMode) {
            startWatchQueryFiles(root.toPath());
        } else {
            xmlParser = null;
        }
    }

    private File getRootDirectory() throws IOException {
        if (devMode) {
            if (root.replaceAll("\\\\", "/").startsWith("/")) {
                return new File(Paths.get(System.getProperty("user.dir") + root).toUri());
            } else {
                return new File(Paths.get(System.getProperty("user.dir") + "/src/main/resources/" + root).toUri());
            }
        } else {
            ClassPathResource path = new ClassPathResource(root);
            return path.getFile();
        }
    }

    private void storeQueryFromFile(Path path, boolean startup) throws IOException, SAXException {
        synchronized (xmlParser) {
            Document document = xmlParser.parse(path.toFile());
            document.getDocumentElement().normalize();
            NodeList queryMap = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < queryMap.getLength(); i++) {
                Node query = queryMap.item(i);
                if ("query".equals(query.getNodeName())) {
                    NamedNodeMap attributes = query.getAttributes();
                    String id = parseAttributeValue(attributes, "id");
                    String name = parseAttributeValue(attributes, "name");
                    String creator = parseAttributeValue(attributes, "creator");
                    String createdDate = parseAttributeValue(attributes, "createdDate");
                    if (startup) {
                        if (queryStore.containsKey(id)) {
                            throw new RuntimeException("already exists Query ID : " + id);
                        }
                        startup = false;
                    }
                    queryStore.put(id, new Query(id, name, creator, createdDate, trim(query.getTextContent()), injectComment));
                }
            }
        }
    }

    private String trim(String text) {
        return text.replaceAll("\n        ", "\n").trim();
    }

    private void startWatchQueryFiles(final Path path) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    while (true) {
                        WatchKey watchKey = watchService.take();
                        List<WatchEvent<?>> watchEventList = watchKey.pollEvents();
                        for (WatchEvent<?> event : watchEventList) {
                            WatchEvent.Kind<?> kind = event.kind();
                            Path target = (Path) event.context();
                            if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                storeQueryFromFile(Paths.get(path.toString(), target.toString()), false);
                            }
                        }
                        if (!watchKey.reset()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (SAXException e) {
                    LOG.error(e.getMessage());
                    startWatchQueryFiles(path);
                }
            }
        });
        thread.start();
    }

    private String parseAttributeValue(NamedNodeMap attributes, String name) {
        Node idAttr = attributes.getNamedItem(name);
        if (idAttr != null) {
            return idAttr.getTextContent();
        }
        return null;
    }

    public Query getQuery(String id) {
        return queryStore.get(id);
    }

}
