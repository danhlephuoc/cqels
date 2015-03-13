CQELS Cloud Performance Tests

# Introduction #

Linked Stream Data extends the Linked Data paradigm to dynamic data sources. It enables the integration and joint processing of heterogeneous stream data sources and data from the Linked Data Cloud. Several Linked Stream Data processing engines exist but their scalability still needs to be in improved in terms of (static and dynamic) data sizes, number of concurrent queries, stream update frequencies, etc. Moreover, none of them yet supports parallel processing in the Cloud, i.e., elastic load profiles in a hosted environment. To remedy these limitations, we present an approach for elastically parallelizing the continuous ex- ecution of queries over Linked Stream Data. For this, we have developed novel and highly efficient and scalable parallel algorithms for continuous query operators. Our approach and algorithms are implemented in our CQELS Cloud system and we present extensive evaluations of its supe- rior performance on Amazon EC2 demonstrating its high scalability and excellent elasticity.

In this tutorial we will describe the required steps for setting up a distributed, multinode CQELS Cloud cluster backed by the Storm, HBase and Hadoop Distributed File System, running on local cluster and Amazon EC2.

# Tutorial approach and structure #

We implemented our elastic execution model and the parallel algorithms using Zookeeper, Storm and HBase. The architecture of CQELS Cloud is is shown in Figure 1: The Execution Coordinator coordinates the cluster of OCs using coordination services provided by Storm and HBase which share the same Zookeeper cluster. The Global Scheduler uses Nimbus3, an open source EC2/S3- compatible Infrastructure-as-a-Service implementation, to deploy the operators’ code to OCs and monitor for failures. Each OC node runs a Storm supervisor which listens for continuous processing tasks assigned to its machine via Nimbus. The processing tasks that need to process the persistent data use the HBase Client component to access data stored in HBase. The machines running an OC also hosts the HDFS Data Nodes of the HBase cluster. The Data Nodes are accessed via the OC’s HRegionServer component of HBase.

![http://cqels.googlecode.com/files/CQELSCloud.png](http://cqels.googlecode.com/files/CQELSCloud.png)

# How-to: Set Up an Apache Hadoop/Apache HBase Cluster on EC2 #

## Prerequisites ##

  * Amazon EC2 account
  * [Whirr-0.8.0-cdh4.2.0](https://cqels.googlecode.com/files/whirr-0.8.0-cdh4.2.0.zip)
  * Storm 0.8.2
  * [CqelsCloud jar](https://cqels.googlecode.com/files/distributed_cqels-0.0.1-SNAPSHOT-jar-with-dependencies.jar)
  * [Test Data](https://cqels.googlecode.com/files/split.zip)

### Step 1: Install EC2 command-line tools ###

You will need to sign up, or create an Amazon Web Service (AWS) account at http://aws.amazon.com/.

We will use EC2 command-line tools to manage our instances. You can download and set up the tools by following the instructions available at the following page:

http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/index.html?SettingUp_CommandLine.html.

You need a public/private key to log in to your EC2 instances. You can generate your key pairs and upload your public key to EC2, using these instructions:

http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/generating-a-keypair.html.

Before you can log in to an instance, you must authorize access. The following link contains instructions for adding rules to the default security group:

http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/adding-security-group-rules.html.

After all these steps are done, review the following checklist to make sure everything is ready:

  * **X.509 certificates:** Check if the X.509 certificates are uploaded. You can check this at your account's Security Credentials page.
  * **EC2 key pairs:** Check if EC2 key pairs are uploaded. You can check this at **AWS Management Console | Amazon EC2 | NETWORK & SECURITY | Key Pairs**.
  * **Access:** Check if the access has been authorized. This can be checked at **AWS Management Console | Amazon EC2 | NETWORK & SECURITY | Security Groups | Inbound**.
  * **Environment variable settings:** Check if the environment variable settings are done. As an example, the following snippet shows my settings; make sure you are using the right EC2\_URL for your region:

```
$ cat ~/.bashrc
export EC2_HOME=~/opt/ec2-api-tools-1.4.4.2
export PATH=$PATH:$EC2_HOME/bin
export EC2_PRIVATE_KEY=~/.ec2/pk-OWRHNWUG7UXIOPJXLOBC5UZTQBOBCVQY.pem
export EC2_CERT=~/.ec2/cert-OWRHNWUG7UXIOPJXLOBC5UZTQBOBCVQY.pem
export JAVA_HOME=/Library/Java/Home
export EC2_URL=https://ec2.us-west-1.amazonaws.com
```
We need to import our EC2 key pairs to manage EC2 instances via EC2 command-line tools:
```
$ ec2-import-keypair your-key-pair-name --public-key-file ~/.ssh/id_rsa.pub
```
Verify the settings by typing the following command:
```
$ ec2-describe-instances
```
If everything has been set up properly, the command will show your instances similarly to how you had configured them in the previous command.

### Step 2: Install Apache Whirr ###

#### Apache Whirr™ ####

Apache Whirr is a set of libraries for running cloud services.

**Whirr provides:**

  * A cloud-neutral way to run services..
  * A common service API. The details of provisioning are particular to the service.
  * Smart defaults for services. You can get a properly configured system running quickly, while still being able to override settings as needed.
  * You can also use Whirr as a command line tool for deploying clusters.

To install Whirr, please make sure that you have Java JDK 6 or 7 installed on your machine.

#### Installation ####
You can download whirr here

Uncompress the archive:
```
tar xvfz whirr-0.8.0-cdh4.2.0.tar.gz
```

Now we are going to write a config file to tell whirr how to deploy hadoop on amazon ec2. Create the file ~/cqelscloud-ec2.properties with the following content:
```
whirr.cluster-name=cqelscloud
whirr.cluster-user=storm
whirr.instance-templates=1 zookeeper+hadoop-namenode+yarn-resourcemanager+mapreduce-historyserver+hbase-master,2 hadoop-datanode+yarn-nodemanager+hbase-regionserver
whirr.provider=aws-ec2
whirr.identity=<AMAZON ACCESS KEY ID>
whirr.credential=<AMAZON SECRET ACCESS KEY>
whirr.private-key-file=${sys:user.home}/.ssh/id_rsa
whirr.public-key-file=${sys:user.home}/.ssh/id_rsa.pub
whirr.env.MAPREDUCE_VERSION=2
whirr.env.repo=cdh4
# whirr.java.install-function=install_oab_java
whirr.java.install-function=install_oracle_jdk6
whirr.zookeeper.install-function=install_cdh_zookeeper
whirr.zookeeper.configure-function=configure_cdh_zookeeper
whirr.hadoop.install-function=install_cdh_hadoop
whirr.hadoop.configure-function=configure_cdh_hadoop
whirr.hbase.install-function=install_cdh_hbase
whirr.hbase.configure-function=configure_cdh_hbase
whirr.mr_jobhistory.start-function=start_cdh_mr_jobhistory
whirr.yarn.configure-function=configure_cdh_yarn
whirr.yarn.start-function=start_cdh_yarn
whirr.hardware-id=m1.medium
whirr.image-id=us-east-1/ami-2efa9d47
whirr.location-id=us-east-1
```

We also need a RSA ssh key pair with no password, to generate it type:
```
ssh-keygen -t rsa -P ''
```
And use the default values.

Now we can deploy hadoop on amazon ec2  using whirr
```
cd whirr-0.8.1/
bin/whirr launch-cluster --config ~/cqelscloud-ec2.properties
```
It will start 2 micro instances on amazon EC2. Then it will install java, hadoop and hbase. On one instance, we’ll have the hmaster, hadoop namenode and the hadoop jobtracker and on the other instance we’ll have the tasktracker and regionserver.
When whirr is done deploying hadoop and hbase, you should get a message like:
```
You can log into instances using the following ssh commands:
'ssh -i /Users/hoan/.ssh/id_rsa -o "UserKnownHostsFile /dev/null" -o StrictHostKeyChecking=no storm@54.242.102.235'
'ssh -i /Users/hoan/.ssh/id_rsa -o "UserKnownHostsFile /dev/null" -o StrictHostKeyChecking=no storm@50.16.173.250'
```

If everything went well, you should be able to see the two instances running on amazon ec2 console: https://console.aws.amazon.com/ec2/home?region=us-east-1#s=Instances

You should also be able to access to the hbase master web interface using your browser:

**http://<hmaster ip>:60010**

# How-to: Set Up Storm on top of HBase EC2 #
Storm is a distributed realtime computation system. Similar to how Hadoop provides a set of general primitives for doing batch processing, Storm provides a set of general primitives for doing realtime computation. Storm is simple, can be used with any programming language, is used by many companies, and is a lot of fun to use!

Nathan Marz, the author of Twitter Storm, provides a solution to deploy Storm cluster on Amazon EC2 platform. The project named storm-deploy. However, storm-deploy can only deploy Storm on a newly created cluster. For that reason, in order to have Hadoop/HBase and Storm to work together, you must deploy these clusters separately and then, connect its to each other.

### Install Storm using Storm-deploy project ###

Deploying Hadoop/HBase and Storm on separated clusters is not recommended. For that reason, this document will not cover the topic. Complete guide to storm-deploy can be found at https://github.com/nathanmarz/storm-deploy/wiki.

### Install Storm using our custom storm-deploy ###

Using default storm-deploy project does not take much efforts and quite simple. However, it cannot deploy Storm on existing cluster. After playing some various solutions around, we decided to write our own custom scripts to have Storm running on top of Hadoop/HBase cluster. This solution is much more efficient and cost-saving than the previous one.

In our custormise Storm deployment, the master node of Storm is also the master node of Hadoop/HBase cluster. Further modifications need to be made to get Storm installed in other architectures.

You can download our custom Storm-deploy here

Uncompress the archive:
```
tar xvfz storm-deploy.2.0.tar.gz
```

Custom Storm deployment consists of 3 scripts:
￼
  * **make-node-list.sh**: copy this script to your Whirr directory. This script will:
    1. Generate a ￼ storm-nodes.list fromlaunch-cluster.log
    1. Each line in the storm-nodes.list￼ is an entry for each node of the cluster:
```
        <role>:<hostname>:<public-ip>:<private-ip> 
        <role>:<hostname>:<public-ip>:<private-ip>
```

  * **Storm-install.sh**: place this script in your $HOME directory. This script will install Storm on each node and start Storm services corresponding to its roles.
  * **Storm-deploy.sh**: place this script in your $HOME directory. What it does are:
    1. Copy storm-nodes.list￼ to each nodes. This helps configuring storm.yaml for Storm cluster.
    1. Traverse each node in the list and remotely execute the storm- install.sh on each target machine to install Storm in parallel.

To deploy Storm cluster from local machine:
  * Generate storm-nodes.list ￼ from launch-cluster.log￼ file:
```
        $ cd /path/to/whirr/directory/
        $ ./make-node-list.sh
```
  * Run storm-deploy.sh on local machine:
```
        $ cd ~/
        $ ./storm-deploy.sh
```

After a few minutes, you can check whether your storm cluster installation is success or not by visiting this URL: http://master-public-ip-or-hostname:8080/

## HOW TO TEST ##
Note: The value after each of argument explanation is the sample value.

### Test Operators separately ###

  * **Operator MATCH**

storm jar Join5w-1.0.0-jar-with-dependencies.jar  main.Match args0  args1  args2 args3 args4  args5

  1. args0: topology name - matchOp
  1. args1: The amount of patterns will be used to check if triple is matched - 100000
  1. args2: The amount of batch data will be sent to match bolt. Batch is defined as a number of triple data packed together - 100000
  1. args3: The number of executors for the match bolt - 64
  1. args4: How many triple data will be packed to 1 batch -  1000
  1. args5: The number of worker will be used for this operator - 32

  * **Operator JOIN**

storm jar Join5w-1.0.0-jar-with-dependencies.jar main.Join0A args0 args1 args2 args3 args4 args5 args6

  1. args0 :  topology name – JoinOAOp
  1. args1: The amount of spout executors – 32
  1. args2: The amount of batch data will be sent to match bolt. Batch is defined as a number of triple data packed together - 100000
  1. args3: How many triple data will be packed to 1 batch -  1000
  1. args4: The number of executors for the join bolt – 64
  1. args5: The size of window buffer – 100000
  1. args6: The amount of workers for this operator - 32

  * **Operator JOIN and AGGREGATION after**

storm jar Join5w-1.0.0-jar-with-dependencies.jar  main.Join1A args0 args1 args2 args3 args4 args5 args6 args7 args8

  1. args0 :  topology name – Join1AOp
  1. args1: The amount of spout executors – 32
  1. args2: The amount of batch data will be sent to match bolt. Batch is defined as a number of triple data packed together - 100000
  1. args3: How many triple data will be packed to 1 batch -  1000
  1. args4: The number of executors for the join bolt – 64
  1. args5: The size of window buffer – 100000
  1. args6: The amount of workers for this operator – 32
  1. args7: The amount of executors for agg threads – 32
  1. args8: The type of aggregation– 3 (for Min aggregation)

  * **Operator AGGREGATION**

> storm jar Join5w-1.0.0-jar-with-dependencies.jar main.Agg  args0  args1  args2  args3 args4  args5  args6  args7

where( the number in the end is the sample value)
  1. args0 :  topology name – AggOp
  1. args1: The amount of spout executors – 32
  1. args2: How many triple data will be packed into 1 batch – 1000.
  1. args3: The amount of batch data will be sent to match bolt. Batch is defined as a number of triple data packed together
  1. args4: The number of executors for the aggregation bolt – 32
  1. args5: The size of window buffer – 100000
  1. args6: The type of aggregation– 3 (for Min aggregation)
  1. args7: The amount of workers for this operator – 32

#### Test 4 queries ####
  * **Query 1**

storm jar distributed\_cqels-0.0.1-SNAPSHOT-jar-with-dependencies.jar  main.Query1 args0  args1  args2 args3

  1. args0: topology name - Query1
  1. args1: The amount of patterns will be used to check if triple is matched – 100000
  1. args2: How many triple data will be packed to 1 batch after match operation -  1000
  1. args3: How many executors for decode task - 32

  * **Query 2**

storm jar distributed\_cqels-0.0.1-SNAPSHOT-jar-with-dependencies.jar  main.Query2 args0  args1  args2 args3 args4  args5

  1. args0: topology name - Query2
  1. args1: The amount of patterns will be used to check if triple is matched – 100000
  1. args2: How many triple data will be packed to 1 batch after match operation -  1000
  1. args3: The number of executors for join bolt - 32
  1. args4: The number of executors for decode bolt - 32
  1. args5: The number of output buffers for multiple M join - 1000

  * **Query 3**

storm jar distributed\_cqels-0.0.1-SNAPSHOT-jar-with-dependencies.jar  main.Query3 args0  args1  args2 args3 args4

  1. args0: topology name - Query3
  1. args1: The amount of patterns will be used to check if triple is matched – 100000
  1. args2: How many triple data will be packed to 1 batch -  1000
  1. args3: How many executors for decode task – 32
  1. args4: The number of output buffers for multiple M join - 1000

  * **Query 4**

storm jar distributed\_cqels-0.0.1-SNAPSHOT-jar-with-dependencies.jar  main.Query4 args0  args1  args2 args3 args4 args5 args6

  1. args0: topology name - Query4
  1. args1: The amount of patterns will be used to check if triple is matched – 100000
  1. args2: How many triple data will be packed to 1 batch after match operator -  1000
  1. args3: How many executors for join task – 32
  1. args4: How many executors for aggregation task – 32
  1. args5: How many executors for decode task – 32
  1. args6: The number of output buffers for multiple M join - 1000