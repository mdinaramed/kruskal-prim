package org.example;

import java.util.*;

public class Kruskal {

    // дорога между районами ребра
    public static class EdgeK {
        public final String from, to;
        public final int weight;
        public EdgeK(String from, String to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }
    // результат работы алгоритма
    public static class Result {
        public final List<EdgeK> mstEdges;
        public final int totalCost;
        public final long operationsCount;
        public final double executionTimeMs;
        Result(List<EdgeK> mstEdges, int totalCost, long operationsCount, double ms) {
            this.mstEdges = mstEdges;
            this.totalCost = totalCost;
            this.operationsCount = operationsCount;
            this.executionTimeMs = ms;
        }
    }
    // считаем количество действий
    private static class Metrics {
        long comparisons = 0;
        long finds = 0;
        long unions = 0;
        long total() {
            return comparisons + finds + unions;
        }
    }
    // DSU — чтобы проверять, соединены ли вершины
    private static class DSU {
        int[] p, r; final Metrics m;
        DSU(int n, Metrics m){
            this.m = m;
            p = new int[n];
            r = new int[n];
            for (int i=0;i<n;i++) p[i]=i; }
        // находит родителя вершины
        int find(int x){
            m.finds++;
            return p[x]==x ? x : (p[x]=find(p[x]));
        }
        // объединяет множества, если они разные
        boolean union(int a,int b){
            int ra = find(a), rb = find(b);
            m.comparisons++;
            if (ra == rb) return false;
            m.unions++;
            m.comparisons++;
            if (r[ra] < r[rb]) p[ra] = rb;
            else if (r[ra] > r[rb]) {
                m.comparisons++; p[rb] = ra;
            } else {
                p[rb] = ra; r[ra]++;
            }
            return true;
        }
    }
    // сам алгоритм Крускала
    public static Result compute(List<String> nodes, List<EdgeK> edges) {
        long t0 = System.nanoTime();
        Metrics m = new Metrics();
        // нумеруем вершины
        Map<String,Integer> idx = new HashMap<>();
        for (int i=0;i<nodes.size();i++) idx.put(nodes.get(i), i);
// сортируем рёбра по весу
        List<EdgeK> sorted = new ArrayList<>(edges);
        sorted.sort((a,b)->{ m.comparisons++; return Integer.compare(a.weight, b.weight); });

        DSU dsu = new DSU(nodes.size(), m);
        List<EdgeK> mst = new ArrayList<>();
        int cost = 0;
// перебираем рёбра
        for (EdgeK e : sorted) {
            int u = idx.get(e.from), v = idx.get(e.to);
            if (dsu.union(u, v)) {
                mst.add(e);
                cost += e.weight;
                if (mst.size() == nodes.size()-1) break;
            }
        }

        long t1 = System.nanoTime();
        double ms = (t1 - t0) / 1_000_000.0;
        return new Result(mst, cost, m.total(), ms);
    }
}