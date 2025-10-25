package org.example;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class App {


    static class InputFile { List<Graph> graphs; }
    static class Graph { int id; List<String> nodes; List<Edge> edges; }
    static class Edge {
        String from, to; int weight;
        Edge() {}
        Edge(String f,String t,int w){ from=f; to=t; weight=w; }
    }


    static class OutputFile { List<Result> results = new ArrayList<>(); }
    static class Result {
        @SerializedName("graph_id") int graphId;
        @SerializedName("input_stats") InputStats inputStats;
        @SerializedName("kruskal") AlgoResult kruskal;
        @SerializedName("prim")    AlgoResult prim;
    }
    static class InputStats { int vertices, edges; }
    static class AlgoResult {
        @SerializedName("mst_edges") List<Edge> mstEdges;
        @SerializedName("total_cost") int totalCost;
        @SerializedName("operations_count") long operationsCount;
        @SerializedName("execution_time_ms") double executionTimeMs;
    }

    static InputFile readInput(String path) throws IOException {
        try (Reader rd = new FileReader(path)) { return new Gson().fromJson(rd, InputFile.class); }
    }
    static void writeOutput(String path, OutputFile out) throws IOException {
        try (Writer wr = new FileWriter(path)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(out, wr);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: App <input.json> <output.json>");
            return;
        }
        String inPath = args[0], outPath = args[1];

        InputFile in = readInput(inPath);
        OutputFile out = new OutputFile();

        for (Graph g : in.graphs) {
            Result res = new Result();
            res.graphId = g.id;

            InputStats st = new InputStats();
            st.vertices = g.nodes.size();
            st.edges = g.edges.size();
            res.inputStats = st;


            List<Kruskal.EdgeK> edgesK = g.edges.stream()
                    .map(e -> new Kruskal.EdgeK(e.from, e.to, e.weight))
                    .collect(Collectors.toList());

            // Kruskal
            Kruskal.Result kr = Kruskal.compute(g.nodes, edgesK);
            AlgoResult arK = new AlgoResult();
            arK.totalCost = kr.totalCost;
            arK.operationsCount = kr.operationsCount;
            arK.executionTimeMs = kr.executionTimeMs;
            List<Edge> mstK = new ArrayList<>();
            for (Kruskal.EdgeK e : kr.mstEdges) mstK.add(new Edge(e.from, e.to, e.weight));
            arK.mstEdges = mstK;
            res.kruskal = arK;

            // Prim
            Prim.Result pr = Prim.compute(g.nodes, edgesK);
            AlgoResult arP = new AlgoResult();
            arP.totalCost = pr.totalCost;
            arP.operationsCount = pr.operationsCount;
            arP.executionTimeMs = pr.executionTimeMs;
            List<Edge> mstP = new ArrayList<>();
            for (Prim.EdgeP e : pr.mstEdges) mstP.add(new Edge(e.from, e.to, e.weight));
            arP.mstEdges = mstP;
            res.prim = arP;

            out.results.add(res);


            System.out.printf(
                    "Graph %d | V=%d E=%d | Kruskal cost=%d (%.3f ms, %d ops) | Prim cost=%d (%.3f ms, %d ops)%n",
                    g.id,
                    st.vertices, st.edges,
                    kr.totalCost, kr.executionTimeMs, kr.operationsCount,
                    pr.totalCost, pr.executionTimeMs, pr.operationsCount
            );

        }

        writeOutput(outPath, out);
        System.out.println("Done. Wrote: " + outPath);
    }
}
