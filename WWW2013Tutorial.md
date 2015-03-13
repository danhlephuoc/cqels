# **Linked Stream Data Processing Tutorial** #

# Introduction #

The processing of streams, specifically in the context of Big Data and analytics, has been in the centre of attention of the research community over the last years. Addressing the 3 Vs - volume, velocity and variety, has sparked interesting new research. The Web community has contributed to this joint research push through Linked Streams, building on the a core strength of the Web, the linking of information and its integrated processing. Given the rapidly growing number of stream data sources (sensors, mobile phones, social network services, etc.). Linked Stream Data enables the simple and seamless integration not only among heterogeneous stream data, but also among streams and Linked Data collections, enabling a new range of real-time applications.
This tutorial gives an overview about Linked Stream Data processing. It describes the basic setup and assumptions, highlighting the challenges any system has to face, such as managing the temporal aspects, temporal semantics, efficient processing, timeliness, etc. It presents the different architectures for Linked Stream Data processing engines, their advantages and disadvantages and reviews the state of the art in Linked Stream Data processing system. The tutorial will discuss in detail the design choices, data structures, and overall performance parameters and their impact on implementations. A short discussion of the current challenges and open problems is also given. In the last part of the tutorial the participants will build their own Linked Stream Data processing application providing hands-on experience of the theoretical discussions in the first part.


# Agenda #

**Part I: Basic Concepts & Modeling (1.5h presentation, [Slides](https://cqels.googlecode.com/files/Linked%20Stream%20Data%20Processing%20Part1.pdf))**
  1. Linked Stream Data
  1. Data models
  1. Query Languages and Operators
  1. Choices and challenges in designing a Linked Stream Data processor

**Part II: Building a Linked Stream Processing Engine (1.5h presentation,[Slides](https://cqels.googlecode.com/files/Linked%20Stream%20Data%20Processing%20Part2.pdf))**
  1. Analysis of available Linked Stream Processing Engines
  1. Design choices, implementation
  1. Performance comparison
  1. Open challenges

# Presenters #

Dr. **Danh Le Phuoc** is a Senior Software Architecture and Postdoctoral Researcher of the Digital Enterprise Research Institute (DERI), at the National University of Ireland, Galway (NUIG). His research interests include semantic data mashups, query processing for mobile devices and Linked Data stream processing. He has been developing [CQELS engine](https://code.google.com/p/cqels/), a native and adaptive stream processing engine for Linked Stream Data. He has contributed to the [Semantic Sensor Network Ontology](http://www.w3.org/2005/Incubator/ssn/ssnx/ssn)  of [W3C Semantic Sensor Network Incubator Group](http://www.w3.org/2005/Incubator/ssn/). Moreover, he has built DERI Semantic Web Pipes, a semantic version of Yahoo! Pipes. He is also leading the developments of [RDF On The Go](https://code.google.com/p/rdfonthego/), a triple storage for Android phones (Best Demo Award at ISWC 2010) and [Linked Sensor Middleware](http://lsm.deri.ie/), a middleware for integrating sensor data into Linked Data Cloud (Honorable Mention at Semantic Web Challenge 2011). He is developing a benchmarking framework for Linked Stream Data Processing engines called [LSBench](https://code.google.com/p/lsbench/). He is also leading the development of CQELS Cloud, the elastic stream processing engine of the Cloud. For building Mashup for Linked Stream Data, he is coordinating the development of [Super Stream Collider](http://superstreamcollider.org/). He served as co-chair, invited speaker and Program Committee of international conferences such as I-Semantics (Triplification Challenge), UBICOMM, ESWC. He has already presented a tutorial on Linked Stream Data processing at the Reasoning Web Summer School 2012.

Prof. **Manfred Hauswirth** is the Vice-Director of the Digital Enterprise Research Institute (DERI), Galway, Ireland and a professor at the National University of Ireland, Galway (NUIG). His research interests are on linked data streams, semantic sensor networks, sensor networks middleware, large-scale semantics-enabled distributed information systems and applications, peer-to-peer systems, Internet of things, self-organization and self-management, service-oriented architectures and distributed systems security. He has published over 160 papers in these domains, he has co-authored a book on distributed software architectures and several book chapters on data management and semantics, and already presented numerous tutorials at top-ranked conferences.


Dr. **Josiane Xavier Parreira** is a project leader in the Digital Enterprise Research Institute (DERI), Galway, Ireland. She holds an M.S. (2003) and a Ph.D. (2009) in computer science from the Max Planck Institute for Informatics (MPII) - University of Saarland, Saarbrucken, Germany. Her main research interests are on semantic sensor networks, large-scale distributed systems, social networks, Internet of things, link analysis and peer-to-peer systems. She is co-developer of the CQELS engine and the Linked Stream Middleware. She has already presented a tutorial on Linked Stream Data processing at the Reasoning Web Summer School 2012.