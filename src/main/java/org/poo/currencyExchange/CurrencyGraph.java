package org.poo.currencyExchange;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class CurrencyGraph {
    private Map<String, Map<String, Double>> graph = new HashMap<>();

    /***
     * Just adding the exchange rates to the graph
     * @param from - the currency we are converting from
     * @param to - the currency we are converting to
     * @param rate - the exchange rate given
     */

    public void addExchangeRate(final String from, final String to, final double rate) {
        graph.putIfAbsent(from, new HashMap<>());
        graph.get(from).put(to, rate);
    }

    /***
     * Method to find the exchange rate between two currencies
     * Using Dijkstra's algorithm to find the exchange rate
     * it's an algorithm that finds the shortest path between two nodes in a graph
     * for bibliography check README.md (i watched some yt vids)
     * @param from - the currency we are converting from
     * @param to - the currency we are converting to
     * @return - the calculated exchange rate
     */
    public double getExchangeRate(final String from, final String to) {

        // priority queue for finding the best rate
        PriorityQueue<ExchangeNode> pq =
                new PriorityQueue<>(Comparator.comparingDouble(node -> node.getRate()));
        pq.add(new ExchangeNode(from, 1.0));

        Map<String, Double> visited = new HashMap<>();
        visited.put(from, 1.0);

        while (!pq.isEmpty()) {
            ExchangeNode current = pq.poll();

            // End = we found the rate
            if (current.getCurrency().equals(to)) {
                return current.getRate();
            }

            if (graph.containsKey(current.getCurrency())) {
                for (Map.Entry<String, Double> neighbor
                        : graph.get(current.getCurrency()).entrySet()) {
                    double newRate = current.getRate() * neighbor.getValue();

                    // if better rate, add to the queue
                    if (!visited.containsKey(neighbor.getKey())
                            || newRate < visited.get(neighbor.getKey())) {
                        visited.put(neighbor.getKey(), newRate);
                        pq.add(new ExchangeNode(neighbor.getKey(), newRate));
                    }
                }
            }
        }

        return -1; // No correct road beetwen the two currencies
    }

}
