#!/usr/bin/env bash
set -euo pipefail

if ! kubectl get ns kafka 2>/dev/null; then
    echo "Creating namespace for Kafka" >&2
    kubectl create ns kafka
fi

echo "Installing Strimzi Kafka operator" >&2
kubectl -n kafka apply -f 'https://strimzi.io/install/latest?namespace=kafka'

echo "Installing a single-node Kafka cluster" >&2
kubectl -n kafka apply -f 'https://strimzi.io/examples/latest/kafka/kafka-persistent-single.yaml'

echo "Waiting for the cluster to start" >&2
kubectl -n kafka wait kafka/my-cluster --for=condition=Ready --timeout=300s

echo "Kafka cluster is available at: my-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092"
