package org.example;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
// главный класс запуск
public class App {
    // структура входного файла input.json
    static class InputFile {
        List<Graph> graphs;
    }
    // граф  список вершин и рёбер
    static class Graph {
        int id;
        List<String> nodes;
        List<Edge> edges;
    }
    // ребро между двумя вершинами
    static class Edge {
        String from, to; int weight;
        Edge() {}
        Edge(String f, String t, int w){
            from=f;
            to=t;
            weight=w;
        }
    }
    // структура выходного файла output.json
    static class OutputFile {
        List<Result> results = new ArrayList<>();
    }
    // результат для каждого графа
    static class Result {
        @SerializedName("graph_id") int graphId;
        @SerializedName("input_stats") InputStats inputStats;
        @SerializedName("kruskal") AlgoResult kruskal;
    }
    // инфо о количестве вершин и рёбер
    static class InputStats {
        int vertices, edges;
    }
    // результат алгоритма
    static class AlgoResult {
        @SerializedName("mst_edges") List<Edge> mstEdges;
        @SerializedName("total_cost") int totalCost;
        @SerializedName("operations_count") long operationsCount;
        @SerializedName("execution_time_ms") double executionTimeMs;
    }
    // чтение входного json
    static InputFile readInput(String path) throws IOException {
        try (Reader rd = new FileReader(path)) {
            return new Gson().fromJson(rd, InputFile.class);
        }
    }
    // запись результата в output.json
    static void writeOutput(String path, OutputFile out) throws IOException {
        try (Writer wr = new FileWriter(path)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(out, wr);
        }
    }
    // основной метод программы
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
            // сохраняем количество вершин и рёбер
            InputStats st = new InputStats();
            st.vertices = g.nodes.size();
            st.edges = g.edges.size();
            res.inputStats = st;
// конвертируем рёбра под Kruskal
            List<Kruskal.EdgeK> edgesK = g.edges.stream()
                    .map(e -> new Kruskal.EdgeK(e.from, e.to, e.weight))
                    .collect(Collectors.toList());
// запускаем алгоритм Крускала
            Kruskal.Result kr = Kruskal.compute(g.nodes, edgesK);

            AlgoResult ar = new AlgoResult();
            ar.totalCost = kr.totalCost;
            ar.operationsCount = kr.operationsCount;
            ar.executionTimeMs = kr.executionTimeMs;
            // рёбра, вошедшие в MST
            List<Edge> mstOut = new ArrayList<>();
            for (Kruskal.EdgeK e : kr.mstEdges) mstOut.add(new Edge(e.from, e.to, e.weight));
            ar.mstEdges = mstOut;

            res.kruskal = ar;
            out.results.add(res);
        }
        // сохраняем результат в файл
        writeOutput(outPath, out);
        System.out.println("Done. Wrote: " + outPath);
    }
}