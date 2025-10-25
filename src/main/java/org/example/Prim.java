package org.example;

import java.util.*;

public class Prim {

    public static class EdgeP {
        public final String from, to;
        public final int weight;

        public EdgeP(String from, String to, int weight){
            this.from=from;
            this.to=to;
            this.weight=weight; }
    }
    public static class Result {
        public final List<EdgeP> mstEdges;
        public final int totalCost;
        public final long operationsCount;
        public final double executionTimeMs;

        Result(List<EdgeP> mstEdges,int totalCost,long operationsCount,double ms){
            this.mstEdges=mstEdges;
            this.totalCost=totalCost;
            this.operationsCount=operationsCount;
            this.executionTimeMs=ms;
        }
    }
    private static class Metrics {
        long pqPush=0,pqPop=0,relax=0;
        long total(){
            return pqPush+pqPop+relax;
        }
    }
    private static class Item implements Comparable<Item> {
        int v,w;
        String parent;

        Item(int v,int w,String parent){
            this.v=v;
            this.w=w;
            this.parent=parent;
        }
        public int compareTo(Item o){
            return Integer.compare(this.w,o.w); }
    }

    public static Result compute(List<String> nodes, List<Kruskal.EdgeK> edges){
        long t0 = System.nanoTime();
        Metrics m = new Metrics();

        int n = nodes.size();
        Map<String,Integer> id = new HashMap<>();
        for (int i=0;i<n;i++) id.put(nodes.get(i), i);

        List<int[]>[] g = new ArrayList[n];
        for (int i=0;i<n;i++) g[i] = new ArrayList<>();
        for (Kruskal.EdgeK e : edges) {
            int u = id.get(e.from), v = id.get(e.to), w = e.weight;
            g[u].add(new int[]{v,w}); g[v].add(new int[]{u,w});
        }

        boolean[] used = new boolean[n];
        List<EdgeP> mst = new ArrayList<>();
        int total = 0;

        for (int s=0;s<n;s++){
            if (used[s]) continue;
            PriorityQueue<Item> pq = new PriorityQueue<>();
            pq.add(new Item(s,0,null)); m.pqPush++;
            while(!pq.isEmpty()){
                Item it = pq.poll(); m.pqPop++;
                if (used[it.v]) continue;
                used[it.v] = true;
                if (it.parent != null){ mst.add(new EdgeP(it.parent, nodes.get(it.v), it.w)); total += it.w; }
                for (int[] e : g[it.v]){
                    int to=e[0], w=e[1];
                    if (!used[to]){ pq.add(new Item(to,w,nodes.get(it.v))); m.pqPush++; m.relax++; }
                }
            }
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        return new Result(mst, total, m.total(), ms);
    }
}
