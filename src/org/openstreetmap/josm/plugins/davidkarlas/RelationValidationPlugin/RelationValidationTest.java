package org.openstreetmap.josm.plugins.davidkarlas.RelationValidationPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;

public class RelationValidationTest extends Test {
    public static final int RELATION_DISCONNECTED = 67546235;

    public RelationValidationTest() {
        super("Relation Analyzer", "Analyzes OSM relations for gaps.");
    }

    class Graph {
        public Set<Node> AllNodes = new HashSet<>();
        public Set<Way> Ways = new HashSet<>();

        public Graph(Way way) {
            way.getNodes().forEach((n) -> AllNodes.add(n));
            Ways.add(way);
        }

        public boolean FastMerge(Way way) {
            List<Node> nodes = way.getNodes();
            Node first = nodes.get(0);
            Node last = nodes.get(nodes.size() - 1);

            if (AllNodes.contains(first) || AllNodes.contains(last)) {
                way.getNodes().forEach((n) -> AllNodes.add(n));
                Ways.add(way);
                return true;
            }
            return false;
        }

        public boolean FastMerge(Graph graph) {
            for (Way way : graph.Ways) {
                List<Node> nodes = way.getNodes();
                if (AllNodes.contains(nodes.get(0)) || AllNodes.contains(nodes.get(nodes.size() - 1))) {
                    graph.AllNodes.forEach((n) -> AllNodes.add(n));
                    graph.Ways.forEach((w) -> Ways.add(w));
                    return true;
                }
            }
            return false;
        }

        public boolean Merge(Graph graph) {
            for (Node node : graph.AllNodes) {
                if (AllNodes.contains(node)) {
                    graph.AllNodes.forEach((n) -> AllNodes.add(n));
                    graph.Ways.forEach((w) -> Ways.add(w));
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void visit(Relation r) {
        List<Graph> graphs = new ArrayList<>();
        for (RelationMember member : r.getMembers()) {
            if (member.getType() == OsmPrimitiveType.WAY) {
                Way way = member.getWay();
                List<Node> nodes = way.getNodes();
                if (nodes.size() < 2) {
                    continue;
                }

                boolean merged = false;
                for (Graph graph : graphs) {
                    if (graph.FastMerge(way)) {
                        merged = true;
                        break;
                    }
                }
                if (merged)
                    continue;

                graphs.add(new Graph(way));
            }
        }
        boolean mergedSomething = false;
        do {
            mergedSomething = false;
            for (Graph graph : graphs.toArray(new Graph[0])) {
                if (!graphs.contains(graph))
                    continue;
                for (Graph graph2 : graphs.toArray(new Graph[0])) {
                    if (graph == graph2)
                        continue;
                    if (!graphs.contains(graph2))
                        continue;

                    if (graph.FastMerge(graph2)) {
                        mergedSomething = true;
                        graphs.remove(graph2);
                    }
                }
            }
        } while (mergedSomething);

        do {
            mergedSomething = false;
            for (Graph graph : graphs.toArray(new Graph[0])) {
                if (!graphs.contains(graph))
                    continue;
                for (Graph graph2 : graphs.toArray(new Graph[0])) {
                    if (graph == graph2)
                        continue;
                    if (!graphs.contains(graph2))
                        continue;

                    if (graph.Merge(graph2)) {
                        mergedSomething = true;
                        graphs.remove(graph2);
                    }
                }
            }
        } while (mergedSomething);

        if (graphs.size() > 1) {
            Set<OsmPrimitive> allLeafs = new HashSet<>();
            int graphsWithAtLeastOneLeaft = 0;
            for (Graph graph : graphs) {
                Set<OsmPrimitive> leafs = CacluateLeafs(graph);
                if (leafs.size() > 0) {
                    graphsWithAtLeastOneLeaft++;
                    allLeafs.addAll(leafs);
                }
            }
            if (graphsWithAtLeastOneLeaft > 1) {
                allLeafs.add(r);
                errors.add(TestError.builder(this, Severity.ERROR, RELATION_DISCONNECTED)
                        .message(tr("Relation is disconnected")).primitives(allLeafs).build());
            }
        }
    }

    Set<OsmPrimitive> CacluateLeafs(Graph graph) {
        List<Node> allNodesAtEndOfWay = new ArrayList<>();
        Set<Node> allNodesInsideWay = new HashSet<>();
        for (Way way : graph.Ways) {
            List<Node> nodes = way.getNodes();
            if (nodes.size() < 2) {
                continue;
            }
            allNodesAtEndOfWay.add(nodes.get(0));
            allNodesAtEndOfWay.add(nodes.get(nodes.size() - 1));
            for (int i = 1; i < nodes.size() - 1; i++) {
                allNodesInsideWay.add(nodes.get(i));
            }
        }
        Set<Node> distinctEndingNodes = new HashSet<>();
        Set<Node> moreThan1EndingNode = new HashSet<>();
        for (Node node : allNodesAtEndOfWay) {
            if (!distinctEndingNodes.add(node)) {
                // OK, this node is 2x in allNodesAtEndOfWay
                // we consider this as connected node...
                moreThan1EndingNode.add(node);
            }
        }
        Set<Node> lonelyNodesAtEnd = new HashSet<>();
        for (Node node : distinctEndingNodes) {
            if (!moreThan1EndingNode.contains(node)) {
                lonelyNodesAtEnd.add(node);

            }
        }
        Set<OsmPrimitive> trulyLonelyNodesAtEnd = new HashSet<>();
        for (Node node : lonelyNodesAtEnd) {
            // Filter out nodes that are inside scanned ways
            if (!allNodesInsideWay.contains(node))
                trulyLonelyNodesAtEnd.add(node);
        }
        return trulyLonelyNodesAtEnd;
    }
}
