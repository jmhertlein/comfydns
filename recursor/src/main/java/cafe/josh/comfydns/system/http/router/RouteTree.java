package cafe.josh.comfydns.system.http.router;

import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

public class RouteTree {
    private final Node root;
    public RouteTree(List<Route> routes) {
        root = new Node();

        for(Route r : routes) {
            Node cur = root;
            if(!r.getPath().isBlank()) {
                for(String pathPart : r.getPath().split("/")) {
                    NodeKey key = new NodeKey(pathPart);
                    Node maybeNext = cur.children.get(key);
                    if(maybeNext == null) {
                        maybeNext = new Node(pathPart);
                        cur.children.put(key, maybeNext);
                    }

                    cur = maybeNext;
                }
            }

            cur.routes.put(r.getMethod(), r);
        }
    }

    public Optional<Routed> route(HttpMethod method, String path) {
        Map<String, String> variables = new HashMap<>();
        while(path.startsWith("/")) {
            path = path.substring(1);
        }
        while(path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        Node cur = root;
        if(!path.isBlank()) {
            for(String pathPart : path.split("/")) {
                NodeKey key = new NodeKey(pathPart);
                Node maybeNext;
                maybeNext = cur.children.get(new NodeKey("{}"));
                if(maybeNext == null) {
                    maybeNext = cur.children.get(key);
                    if(maybeNext == null) {
                        return Optional.empty();
                    }
                } else {
                    variables.put(maybeNext.nodeName, pathPart);
                }

                cur = maybeNext;
            }
        }

        return Optional.of(new Routed(cur.routes.get(method), variables));
    }

    private static class NodeKey {
        public final String token;

        public NodeKey(Node n) {
            if(n.type == NodeType.VARIABLE) {
                token = "{}";
            } else {
                token = n.nodeName;
            }
        }

        public NodeKey(String pathPart) {
            if(pathPart.startsWith("{") && pathPart.endsWith("}")) {
                token = "{}";
            } else {
                token = pathPart;
            }
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            NodeKey nodeKey = (NodeKey) o;
            return Objects.equals(token, nodeKey.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }
    }

    private static class Node {
        public final NodeType type;
        public final Map<NodeKey, Node> children;
        public final Map<HttpMethod, Route> routes;
        public final String nodeName;

        public Node() {
            this.nodeName = "/";
            this.type = NodeType.ROOT;
            this.children = new HashMap<>();
            this.routes = new HashMap<>();
        }

        public Node(String pathPart) {
            if(pathPart.startsWith("{") && pathPart.endsWith("}")) {
                this.type = NodeType.VARIABLE;
                this.nodeName = pathPart.substring(1, pathPart.length()-1);
            } else {
                this.type = NodeType.STATIC;
                this.nodeName = pathPart;
            }

            this.children = new HashMap<>();
            this.routes = new HashMap<>();
        }
    }

    private static enum NodeType {
        ROOT, STATIC, VARIABLE;
    }
}
