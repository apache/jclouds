# The jclouds provider for Windows Azure's Storage (http://www.microsoft.com/windowsazure/storage/default.aspx).
#
* **TODO**: Implementation status.
* **TODO**: Supported features.
* **TODO**: Usage example.

## Running live tests

Try

```sh
mvn clean install -Plive -pl :azureblob -Dtest=AzureBlobClientLiveTest -Dtest.azureblob.identity==<azure_storage_account_name> -Dtest.azureblob.credential=<azure_storage_account_access_key>
```
