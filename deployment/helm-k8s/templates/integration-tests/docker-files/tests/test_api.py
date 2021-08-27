import os
import requests

CLUSTER_ADDR = 'siembol-config-editor-rest'
BASE_URL =f"http://{CLUSTER_ADDR}:8081"


def test_metrics():
    path = "metrics"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200

def test_info():
    path = "info"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.json()['env']['java']['app'].get('description') == 'Siembol config editor'
    assert response.status_code == 200


def test_health():
    path = "health"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.json().get('status') == 'UP'
    assert response.status_code == 200

def test_alert_configs_win_mimikatz():
    service = "alert"
    path = f"api/v1/{service}/configstore/configs"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200
    assert response.json()['files'][0]['file_name'] == "win_mimikatz.json"
    assert response.json()['files'][0]['content'].get('source_type') == "win_eventlogs"

def test_alert_configs_aws_failed_auth():
    service = "alert"
    path = f"api/v1/{service}/configstore/configs"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200
    assert response.json()['files'][2]['file_name'] == "aws_failed_authentication.json"
    assert response.json()['files'][2]['content'].get('source_type') == "aws_cloudtrail"

def test_alert_testcases_mimikatz():
    service = "alert"
    path = f"api/v1/{service}/configstore/testcases"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.json()['files'][0]['content'].get('test_specification').get('event').get('ProcessName') == "C:\\mimikatz.exe"
    assert response.status_code == 200

def test_alert_release_aws_failed_auth():
    service = "alert"
    path = f"api/v1/{service}/configstore/release"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200
    assert response.json()['files'][0]['file_name'] == "rules.json"
    assert response.json()['files'][0]['content'].get('rules')[0].get('rule_description') == "AWS failed authentication"

def test_alert_adminconfig():
    service = "alert"
    path = f"api/v1/{service}/configstore/adminconfig"
    response = requests.get(f'{BASE_URL}/{path}')
    expected = {
        "zk.url": "siembol-zookeeper:2181",
        "zk.path": "/siembol/alerts",
        "zk.base.sleep.ms": 1000,
        "zk.max.retries": 3
    }
    assert response.json()['files'][0]['content']['zookeeper.attributes'] == expected
    assert response.status_code == 200

def test_alert_release_status():
    service = "alert"
    path = f"api/v1/{service}/configstore/release/status"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200
    assert response.json() == {'pull_request_pending': False}

def test_alert_adminconfig_status():
    service = "alert"
    path = f"api/v1/{service}/configstore/adminconfig/status"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200
    assert response.json() == {'pull_request_pending': False}
   
def test_alert_topologies():
    service = "alert"
    path = f"api/v1/{service}/topologies"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.json()['topologies'][0].get('topology_name') == 'siembol-alerts'
    assert response.json()['topologies'][0].get('service_name') == 'alert'
    assert response.status_code == 200

def test_alert_configs_schema():
    service = "alert"
    path = f"api/v1/{service}/configs/schema"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200

def test_alert_configs_testschema():
    service = "alert"
    path = f"api/v1/{service}/configs/testschema"
    response = requests.get(f'{BASE_URL}/{path}')
    expected = {'test_schema': {'type': 'object', 'description': 'Specification for testing alerting rules', 'title': 'alerts test specification', 'properties': {'event': {'type': 'object', 'description': 'Event for alerts evaluation', 'title': 'json raw string'}}, 'required': ['event']}}
    response.json() == expected
    assert response.status_code == 200

def test_alert_configs_importers():
    service = "alert"
    path = f"api/v1/{service}/configs/importers"
    response = requests.get(f'{BASE_URL}/{path}')
    assert response.status_code == 200

def test_testcases_schema():
    path = f"api/v1/testcases/schema"
    response = requests.get(f'{BASE_URL}/{path}')
    expected =  {
        'type': 'string',
        'description': 'The name of the test case',
        'pattern': '^[a-zA-Z0-9_\\-]+$',
        'widget': {
          'formlyConfig': {
            'hideExpression': 'model.version !== 0'
          }
        }
    }
    assert response.json()['rules_schema'].get('properties').get('test_case_name') == expected
    assert response.status_code == 200