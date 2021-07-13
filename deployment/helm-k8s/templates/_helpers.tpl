{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "siembol.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "siembol.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "siembol.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a fully qualified config editor ui fullname.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "siembol.ui.fullname" -}}
{{- $name := default .Chart.Name .Values.ui.appName -}}
{{- printf "%s-%s" (include "siembol.fullname" .) $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a fully qualified config editor rest fullname.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "siembol.rest.fullname" -}}
{{- $name := default .Chart.Name .Values.rest.appName -}}
{{- printf "%s-%s" (include "siembol.fullname" .) $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a fully qualified Service Account and App fullname for the Topology Manager.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "siembol.manager.serviceaccount.fullname" -}}
{{- $name := default .Chart.Name .Values.manager.serviceAccount -}}
{{- printf "%s-%s" (include "siembol.fullname" .) $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "siembol.manager.appname.fullname" -}}
{{- $name := default .Chart.Name .Values.manager.appName -}}
{{- printf "%s-%s" (include "siembol.fullname" .) $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "siembol.labels" -}}
helm.sh/chart: {{ include "siembol.chart" . }}
{{ include "siembol.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "siembol.selectorLabels" -}}
app.kubernetes.io/name: {{ include "siembol.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}


{{/*
Set the nimbus name for the Storm chart
*/}}
{{- define "storm.nimbus.fullname" -}}
{{- printf "%s-%s" .Release.Name "storm-nimbus" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Set the ZooKeeper server for the siembol chart
*/}}

{{- define "siembol.zookeeper.server" -}}
{{- if .Values.zookeeper.enabled -}}
{{- printf "%s-%s" .Release.Name "zookeeper" | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $nullcheck := required "If not using the siembol chart's built-in Zookeeper (i.e. `.Values.zookeeper.enabled: false`), `.Values.zookeeper.servers` is required" .Values.zookeeper.servers -}}
{{- tpl (.Values.zookeeper.servers | toYaml) $ -}}
{{- end -}}
{{- end -}}

{{/*
Set the name for the Kafka chart
*/}}
{{- define "kafka.fullname" -}}
{{- printf "%s-%s" .Release.Name "kafka" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
