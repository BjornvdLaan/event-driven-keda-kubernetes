# Demo instructions

## Preparation

### 1. Required software
You need to have the following installed:
- `kubectl`, a cli-tool to manage a Kubernetes cluster.
    Installation guide is found [here](https://kubernetes.io/docs/tasks/tools/#kubectl).
- `helm`, a package manager for Kubernetes.
    Installation guide is found [here](https://helm.sh/docs/intro/install/).

### 2. Kubernetes cluster
You also need access to a Kubernetes cluster.
One option is to run a cluster locally with [kind](https://kubernetes.io/docs/tasks/tools/#kind):

```bash 
kind create cluster --config deployment/kind-nodeport-config.yml
```

### 3. Install software in the cluster

Metrics Server
```bash
helm repo add metrics-server https://kubernetes-sigs.github.io/metrics-server/
helm repo update
helm upgrade --install --set args={--kubelet-insecure-tls} metrics-server metrics-server/metrics-server --namespace kube-system
```

Set up monitoring:
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add stable https://charts.helm.sh/stable
helm repo update
```

```bash
helm install kind-prometheus prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace \
  --set prometheus.service.nodePort=30000 \
  --set prometheus.service.type=NodePort \
  --set grafana.service.nodePort=31000 \
  --set grafana.service.type=NodePort \
  --set prometheus-node-exporter.service.nodePort=32001 \
  --set prometheus-node-exporter.service.type=NodePort
```

Set up KEDA and Localstack:
```bash
helm repo add kedacore https://kedacore.github.io/charts
helm repo add localstack https://localstack.github.io/helm-charts
```

```bash
helm install keda kedacore/keda --namespace keda --version 2.11.1 --create-namespace
helm install localstack localstack/localstack
```

Build and load Docker images:
```bash
docker build -t taskconsumer:latest consumer
docker build -t taskproducer:latest producer
```

If you use `kind`, then you should load the images as well:
```bash
kind load docker-image taskproducer:latest
kind load docker-image taskconsumer:latest
```

[Optional] Create the queue manually:
The producer and consumer create the queue on startup if it does not exist yet.
However, you can also do it manually if needed:
```bash
kubectl rollout status deployment localstack -n default --timeout=90s
aws sqs --endpoint-url=http://localhost:4566 create-queue --queue-name task-queue
```

## Running the demo
Add producer to fill the queue
```bash
kubectl apply -f deployment/taskproducer.yaml
```

See number of messages in queue
```bash
awslocal sqs get-queue-attributes --queue-url http://localhost:4566/000000000000/task-queue --attribute-names All
```

Add consumer to consume from queue with 1 replica
```bash
kubectl apply -f deployment/taskconsumer.yaml
```

Add KEDA to make num of replicas depend on the queue
```bash
kubectl apply -f deployment/kedascaler.yaml
```

Remove consumer again to let queue fill up
```bash
kubectl delete deploy taskconsumer
```

Remove producer again to see KEDA scale down
```bash
kubectl delete deploy taskproducer
```

Scale up producer to fill up more quickly
```bash
kubectl scale --replicas=10 deployment/taskproducer
```

Scale down producer to fill up more slowly
```bash
kubectl scale --replicas=1 deployment/taskproducer
```

## Extra: Metrics Server and External Metrics Server

### 'Regular' Metrics Server
Get metrics for cpu and memory:
```bash
kubectl top pod taskconsumer-<POD ID>
```

Get metrics as raw json data:
```bash
kubectl get --raw "/apis/metrics.k8s.io/v1beta1/namespaces/default/pods/taskconsumer-<POD ID>" | jq .
```

### KEDA External Metrics Server
Get name of the queue metric:
```bash
kubectl get so taskconsumer-scaler -o jsonpath={.status.externalMetricNames}
```

Get value of metric from external server:
```bash
kubectl get --raw "/apis/external.metrics.k8s.io/v1beta1/namespaces/default/s0-aws-sqs-task-queue?labelSelector=scaledobject.keda.sh/name=taskconsumer-scaler" | jq .
```


