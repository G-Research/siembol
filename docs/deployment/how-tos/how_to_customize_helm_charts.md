How to Customize Helm charts
================================================

When you use the Siembol chart or other charts such as Storm, Zookeeper etc. some configuration options can be limited for your use case. If you need to customise the deployments in ways of your own, you might fork the chart to create your own custom version. If you do this, each time the maintainers update their Helm chart, your custom version becomes out of sync and possibly obsolete. To keep your version up-to-date, you would need to pull from upstream for every update.

#### Can you customize a Helm chart without forking?
Yes, with [Kustomize](https://kustomize.io/), you can use it to perform custom deployments while always using the latest Helm chart version from your vendor. Kustomize enables you to overlay your own 'kustomizations' in yaml files. We have used it by first rendering the chart template locally, and then applying the Kustomize overlay when we deploy the app. This is very useful when deploying the same app to multiple environments, but with different combinations of requirements for each environment. For example a certain port or label is different for dev and prod environments, and in these scenarios it may be more flexible to apply a different Kustomize overlay to the same rendered Helm chart for each environment. Example:

1. Render Helm chart using helm template command:
```bash
$ helm template storm --values values.qa.yaml . > new_templates/temp.yaml
```
The above command outputs a YAML file with all values from the values.yaml file resolved for the templates.

2. Create a new kustomization file to add e.g. a label to a deployment:
```bash
$ cat new_templates/kustomization.yaml
commonLabels:
    app: kustomLabel
resources:
- new_templates/temp.yaml
```
3. Install our chart with the new label:
```bash
$ kubectl apply -k new_templates/.
```
4. We can see that our own kustomization has been applied and deployed together with the upstream chart:
```bash
$ kubectl get deploy storm-ui --show-labels
```
```
NAME     READY  UP-TO-DATE AVAILABLE AGE LABELS
storm-ui   1/1    1            1     10s   app=storm-ui,chart=storm-1.0.14,heritage=Helm,app=kustomLabel
```


