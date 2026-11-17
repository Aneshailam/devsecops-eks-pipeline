# Prometheus and Grafana on EKS

Installed with the kube-prometheus-stack Helm chart.

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

kubectl create namespace monitoring

helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  -f monitoring/values.yaml
```

## Check it is running

```bash
kubectl get pods -n monitoring
kubectl get svc -n monitoring
```

## Open Grafana

The `values.yaml` exposes Grafana through a LoadBalancer. Get its address:

```bash
kubectl get svc monitoring-grafana -n monitoring
```

Log in with user `admin` and the password you set in `values.yaml`
(change it before use, and never commit a real password).

## Useful dashboards

- Kubernetes / Compute Resources / Namespace (Pods)
- Kubernetes / Compute Resources / Node
- Select namespace `default` to see the `devsecops-demo` pods
