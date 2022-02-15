# Deploying Siembol on AWS EKS
### Working Notes & References 
#### Notes
Starting with the Kubernetes version 1.23 launch, officially published **Amazon EKS AMIs will include containerd as the only runtime.** Kurbernetes version 1.17 thru 1.21 use Docker as the default runtime, but have a bootstrap flag option that lets you test out your workloads on any supported cluster today with containerd. For more information, see  [Dockershim deprecation](https://docs.aws.amazon.com/eks/latest/userguide/dockershim-deprecation.html) .

***When an Amazon EKS cluster is created, the IAM entity (user or role) that creates the cluster is added to the Kubernetes RBAC authorization table as the administrator (with system:masters permissions).*** 

**working with multiple aws accounts**
$ aws configure —profile account1
$ aws configure —profile account2

**minikube deploy**
cert-manager
default 
ingress-nginxd
kube-node-lease
kube-public       
kube-system       
siembol           

**Infrastructure as Code** - terraform / polumi to package AWS deployment of Siembol

#### References
[Using Minikube to Create a Cluster | Kubernetes](https://kubernetes.io/docs/tutorials/kubernetes-basics/create-cluster/cluster-intro/)
[Deploying An Application On Kubernetes From A to Z](https://www.magalix.com/blog/deploying-an-application-on-kubernetes-from-a-to-z)
[Fully Deleting AWS EKS Clusters. How to ensure the entire AWS EKS… | by Nick Gibbon | Pareture | Medium](https://medium.com/pareture/fully-deleting-aws-eks-clusters-68074a0dbade)
AWS CLI [Getting started with the AWS CLI - AWS Command Line Interface](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-multiple-profiles)
[Installing kubectl - Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/install-kubectl.html)
[install eksctl command line utility - Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html)
[Create a kubeconfig for Amazon EKS - Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/create-kubeconfig.html) 


- - - -
## Prerequisites
- `kubectl`
- `eksctl`   [The eksctl command-line utility - Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html)
- AWS IAM credentials configured
	- Note: EKS uses IAM for authentication, K8 RBAC for authorization
- AWS account
- AWS VPC

## Step 1: Create Amazon EKS cluster and nodes
```
eksctl create cluster \
--name siembol \ 
--version v1.21.2 \ 
--region us-east-2 \ 
--nodegroup siembol-test \ 
--node-type t2.micro \
--nodes 3
```

To use a configuration file, run the following command 
`eksctl create cluster -f FILE_NAME.yaml`

```
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: siembol
  region: us-east-2

nodeGroups:
  - name: siembol-ng
    instanceType: t2.micro
    desiredCapacity: 3
    ssh: allow
```


### Create Cluster Control Plan
with IAM role chose cluster names, 


### Create worker nodes and connect to cluster -EC2
Create a node group, rather than seperate EC2 instances
Define security, type, resources, etc
Node group - autoscaling
set min and max nodes

### Connnect to cluster from local using kubectl


1. Set up cluster
2. Deploy helm chart 
3. architecture 
4. deploying services
5. networking
6. create a pod to host the application container 
7. container management 
8. expose the application - using service and ingress
9. all cluster files go in source control
10. git add 


#### Kafka
Kafka is a stateful service, and this does make the Kubernetes configuration more complex than it is for stateless microservices. The biggest challenges will happen when configuring storage and network, and you’ll want to make sure both subsystems deliver consistent low latency.

https://www.confluent.io/blog/apache-kafka-kubernetes-could-you-should-you/
