wkhomeControllers.controller('authorVal', ['$rootScope','$scope', 'cookies' ,'$routeParams', '$window', 'globalData', 'profileval', 'saveprofile' ,
  function($rootScope , $scope, cookies, $routeParams, $window, globalData, profileval , saveprofile) {
    // Define a new author object
   
    var author = $routeParams.authorId ;
    var orcid = "";
    var atk = "";
   // console.log ("ROOT");
   // console.log ($rootScope.valor);
       

     // cookies.set ( globalData.getSession , '{ "name" : "asdasd" , "orcid" : "1234-45687" , "access_token" : "789545645621213"}');
    // var logg = cookies.get(oappn + '_ORCID');
    var logg = globalData.getSession();
    var lo = logg !== undefined && logg !== null && logg !== '';
     if (lo){
      $scope.name = JSON.parse(logg).name;
      $scope.orcid = JSON.parse(logg).orcid;
      $scope.atk = JSON.parse(logg).access_token;
     } 
      orcid = $scope.orcid;
       atk = $scope.atk;
    console.log ("Session");
    console.log ($scope.orcid);
    console.log ( $scope.atk);

    console.log (author);

     /* saveprofile.querySrv({data: "{'algo': 'sd'}",'id' : orcid , 'uri' : author , 'atk' : atk }, function (resp) { 
               console.log ("STATUS");
               console.log (resp);
         });*/
        // $('#exampleModal').modal();

          function saveprofi (){

         alert ("Save internal");
          } 

     var authorprofile = '{  "head" : {   "vars" : [ "object", "names", "lbls" ]  },  "results" : {   "bindings" : [ {    "object" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_ESPINOZA_MEJIA_____JORGE_MAURICIO_"    },    "names" : {     "type" : "literal",     "value" : "ESPINOZA MEJIA ,  JORGE MAURICIO"    }   }, {    "object" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/ESPINOZA_MEJIA__M"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejia, M;Espinoza Mejía, M"    },    "lbls" : {     "type" : "literal",     "value" : "APLICACIONES INTERACTIVAS;GINGA-NCL;GUIA DE PROGRAMACION ELECTRONICA;ISDB-TB;LABORATORIO DE TELEVISION DIGITAL;ONTOLOGIAS;PROPIEDADES SEMANTICAS;REGISTRO DE ACTIVIDAD DEL USUARIO;SERVIDOR DE APLICACIONES;SISTEMAS DE RECOMENDACION SEMANTICOS;TELEVISION DIGITAL;TELEVISION DIGITAL TERRESTRE;WEB SEMANTICA"    }   }, {    "object" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/ESPINOZA__MAURICIO"    },    "names" : {     "type" : "literal",     "value" : "Espinoza, Mauricio"    },    "lbls" : {     "type" : "literal",     "value" : "ANOTACIONES SEMANTICAS;APACHE HIVE;APACHE SERVICEMIX;ARQUITECTURA DE SISTEMAS;BIG DATA;CONSULTAS FEDERADAS;CUMULOSRDF;D2RQ;DATOS HIDRO-METEOROLOGICOS;EXPLOTACION DE DATOS;FUENTES SEMI-ESTRUCTURADAS;GEO LINKED DATA;GEOSPARQL;INTEGRACION DE DATOS;ISTAR;METODO DHARMA;METODOLOGIA NEON;NOSQL;ONTOLOGIAS MEDICAS;RDF;RED DE ONTOLOGIAS;SEGMENTACION;SISTEMAS DE INFORMACION;TRANSFORMACION AUTOMATICA A RDF;VISUALIZADOR 3D BASADO EN WEB;WEB SEMANTICA"    }   }, {    "object" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/ESPINOZA_MEJIA__JORGE_MAURICIO"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, Jorge Mauricio"    },    "lbls" : {     "type" : "literal",     "value" : "AUDIO CONTENT ANALYSIS;AUTOMATIC AUDIO SEGMENTATION;AUTOMATIC SEMANTIC ANNOTATION;AUTOMATIC SPEECH RECOGNITION;COLD-START;CONTEXT ACTORS;CONTEXT MODELS;DATA INTEGRATION;DATA MINING;DEMOGRAPHIC STEREOTYPING;DICOM ONTOLOGY;DIGITAL REPOSITORIES;ECUADORIAN UNIVERSITIES;EPG;FEDERATED QUERIES;LINKED DATA;LINKED HEALTH DATA CLOUD;MICROFORMATOS;NLP;ONTOLOGIES;ONTOLOGY;PYTHON;QUERY LANGUAGES;RDF-IZATION;SEMANTIC ANNOTATIONS;SEMANTIC ENRICHMENT;SEMANTIC RECOMMENDER SYSTEM;SEMANTIC REPOSITORY;SEMANTIC WEB;SITIOS WEB;SPARQL;SPEECH TO TEXT;STRATEGIC DEPENDENCY;VIRTUAL INTEGRATION;VISIBILIDAD SITIOS WEB;VISUALIZATION;VOLUMETRIC IMAGE;WEB 3D-VISUALIZER;WEB SEMANTICA;WEB SERVICES"    }   }, {    "object" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/ojs/ESPINOZA-MEJIA__MAURICIO"    },    "names" : {     "type" : "literal",     "value" : "Espinoza-Mejía, Mauricio"    },    "lbls" : {     "type" : "literal",     "value" : "MICROFORMATOS;MICRO-FORMATS;SEMANTIC WEB;VISIBILIDAD SITIOS WEB;WEB SEMÁNTICA;WEBSITE VISIBILITY"    }   }, {    "object" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/ojs/ESPINOZA-MEJIA__M."    },    "names" : {     "type" : "literal",     "value" : "Espinoza-Mejía, M."    }   }, {    "object" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/ojs/ESPINOZA__MAURICIO"    },    "names" : {     "type" : "literal",     "value" : "Espinoza, Mauricio"    },    "lbls" : {     "type" : "literal",     "value" : "ANOTACIONES SEMÁNTICAS;APACHE HIVE;APACHE SERVICEMIX;ARQUITECTURA DE SISTEMAS;AUTOMATIC TRANSFORMATION TO RDF;BIG DATA;CONSULTAS FEDERADASABSTRACTIN THIS ARTICLE;CUMULOSRDF;CUMULUSRDF;D2RQ;DATA DISCOVERY;DATA INTEGRATION;DATOS HIDRO-METEOROLÓGICOS;DHARMA METHOD;EXPLOTACIÓN DE DATOS;FEDERAL QUERIES;FUENTES SEMI-ESTRUCTURADAS;GEO LINKED DATA;GEOSPARQL;HYDRO-METEOROLOGICAL DATA;INFORMATION SYSTEMS;INTEGRACIÓN DE DATOS;IN TUNE WITH CURRENT TRENDS RELATIVE TO DATA INTEGRATION FROM DIFFERENT SOURCES AND THE PUBLICATION OF THESE DATA BY APPLYING THE PRINCIPLES OF LINKED DATA;ISTAR;MÉTODO DHARMA;METODOLOGÍA NEON;NEON METHODOLOGY;NETWORK ONTOLOGIES;NOSQL;ONTOLOGÍAS MÉDICAS;ONTOLOGY BIOMEDICAL;RDF;RED DE ONTOLOGÍAS;SEGMENTACIÓN;SEGMENTATION;SEMANTIC ANNOTATIONS;SEMANTIC WEB;SEMI- STRUCTURED SOURCES;SISTEMAS DE INFORMACIÓN;SYSTEMS ARCHITECTURE;TRANSFORMACIÓN AUTOMÁTICA A RDF;VISUALIZADOR 3D BASADO EN WEB;WEB 3D-VISUALIZER;WEB SEMÁNTICA;WE PRESENT AN APPROACH FOR EXPLOITING THE INFORMATION IN THE ECUADORIAN GEO-HYDROLOGICAL DOMAIN"    }   } ]  } }';
     var authorname = '{  "head": {   "vars": [    "name"   ]  },  "results": {   "bindings": [    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "Mauricio Espinoza"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "M Espinoza"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "M., Espinoza"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "Espinoza , Mauricio"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "Espinoza Mejía, Jorge Mauricio"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "Espinoza, Mauricio"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "Espinoza Mejía, M"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "Espinoza Mejia, M"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "ESPINOZA MEJIA ,  JORGE MAURICIO"     }    },    {     "name": {      "datatype": "http://www.w3.org/2001/XMLSchema#string",      "type": "literal",      "value": "mauricio espinoza"     }    }   ]  } }';
     var publication = '{  "head" : {   "vars" : [ "pub", "ts", "names" ]  },  "results" : {   "bindings" : [ {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/253a7297ad8073e56947363e5fe23302"    },    "ts" : {     "type" : "literal",     "value" : "A robust video identification framework using perceptual image hashing;A Robust Video Identification Framework using Perceptual Image Hashing;A robust video identification framework using perceptual image hashing."    },    "names" : {     "type" : "literal",     "value" : "Oswaldo Francisco Vega;Jose Ramon, Medina;Espinoza Mejía, M;Jose, Medina;Daniel Mendoza;Jose R., Medina;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/6d46a96f158ddff8a544625018dbde33"    },    "ts" : {     "type" : "literal",     "value" : "Towards a Multi-Screen Interactive Ad Delivery Platform;Towards a multi-screen interactive ad delivery platform;Towards a multi-screen interactive ad delivery platform."    },    "names" : {     "type" : "literal",     "value" : "Oswaldo Francisco Vega;kenneth samuel palacio baus;Jose Ramon, Medina;Espinoza Mejía, M;Jose, Medina;Daniel Mendoza;Jose R., Medina;k palaciobaus;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/493ed1f24d0073a548694fd752cb6a4b"    },    "ts" : {     "type" : "literal",     "value" : "WebMedSA: a web-based framework for segmenting and annotating medical images using biomedical ontologies;WebMedSA: A web-based framework for segmenting and annotating medical images using biomedical ontologies"    },    "names" : {     "type" : "literal",     "value" : "Oswaldo Francisco Vega;Alexander Lazovik;Espinoza Mejía, M;Perez , Wilson;Marco Andrés Tello Guerrero;l solanoquinde;Maria Esther, Vidal;SAQUICELA GALARZA , VICTOR HUGO;Lizandro D Solano-Quinde"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/d0216a6393eaa673a50cba5a4ce51cee"    },    "ts" : {     "type" : "literal",     "value" : "Audio fingerprint parameterization for multimedia advertising identification"    },    "names" : {     "type" : "literal",     "value" : "Oswaldo Francisco Vega;Jose Ramon, Medina;Espinoza Mejía, M;Jose, Medina;Daniel Mendoza;Jose R., Medina;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/a95f0ec762d2bce9ae74b4333878bc83"    },    "ts" : {     "type" : "literal",     "value" : "Mobile teleradiology system suitable for m-health services supporting content and semantic based image retrieval on a grid infrastructure.;Mobile teleradiology system suitable for m-health services supporting content and semantic based image retrieval on a grid infrastructure"    },    "names" : {     "type" : "literal",     "value" : "Oswaldo Francisco Vega;Alexander Lazovik;Jose Ramon, Medina;Espinoza Mejía, M;Perez , Wilson;Medina, R.;l solanoquinde;Maria Esther, Vidal;SAQUICELA GALARZA , VICTOR HUGO;Lizandro D Solano-Quinde;Ochoa,Blanca"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/29e578107f9f74ae3a9ab21765b9b4de"    },    "ts" : {     "type" : "literal",     "value" : "Automatic Speech-to-Text Transcription in an Ecuadorian Radio Broadcast Context;Automatic speech-to-text transcription in an ecuadorian radio broadcast context"    },    "names" : {     "type" : "literal",     "value" : "Oswaldo Francisco Vega;Medina, J;Jose Ramon, Medina;Espinoza Mejía, M;Jose, Medina;Jose R., Medina;SAQUICELA GALARZA , VICTOR HUGO;Erik, Sigcha"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/4a93c28407271bfa35aaa32419999b2c"    },    "ts" : {     "type" : "literal",     "value" : "Integration and massive storage of hydro-meteorological data combining big data & semantic web technologies;Integration and massive storage of hydro-meteorological data combining big data semantic web technologies"    },    "names" : {     "type" : "literal",     "value" : "FREIRE ZURITA ,  RENAN GONZALO;DIUC;Universidad de Cuenca;Espinoza Mejía, M;Marco Andrés Tello Guerrero;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/e79f425cad2357b556ffdca84f5c3744"    },    "ts" : {     "type" : "literal",     "value" : "Automatic RDF-ization of big data semi-structured datasets"    },    "names" : {     "type" : "literal",     "value" : "FREIRE ZURITA ,  RENAN GONZALO;Espinoza Mejía, M;Ronald Marcelo Gualan;Marco Andrés Tello Guerrero;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/45602c47cd377b8f85e7141b72c68799"    },    "ts" : {     "type" : "literal",     "value" : "A subject syllabus similarity analysis to address students mobility issue"    },    "names" : {     "type" : "literal",     "value" : "P Vanegas;N Piedra;Espinoza Mejía, M;john fernando baculima;SAQUICELA GALARZA , VICTOR HUGO;Orellana , Gerardo;Piedra Pullaguari, N.O.;Marcos Eugenio Orellana Campoverde"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/9f3112978dad2c70671094c1215c33ea"    },    "ts" : {     "type" : "literal",     "value" : "Towntology & hydrontology: Relationship between urban and hydrographic features in the geographic information domain"    },    "names" : {     "type" : "literal",     "value" : "Miguel A., Bernabé;Espinoza Mejía, M;Vilches,Luis M;Antonio F., Rodríguez Pascual;Mari Carmen, Suárez-Figueroa"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/01575b544604644db9e82af739c8daa0"    },    "ts" : {     "type" : "literal",     "value" : "RDF-ization of DICOM Medical Images towards Linked Health Data Cloud;Rdf-ization of dicom medical images towards linked health data cloud;RDF-ization of DICOM medical images towards linked health data cloud"    },    "names" : {     "type" : "literal",     "value" : "Alexander Lazovik;Espinoza Mejía, M;Marco Andrés Tello Guerrero;Maria Esther, Vidal;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/5a43af001af1f382007adb171fea6ce3"    },    "ts" : {     "type" : "literal",     "value" : "Plataforma para la búsqueda por contenido visual y semántico de imágenes médicas"    },    "names" : {     "type" : "literal",     "value" : "Alexander Lazovik;DIUC;Universidad de Cuenca;Espinoza Mejía, M;Marco Andrés Tello Guerrero;washintong ramirezmontalvan;l solanoquinde;Maria Esther, Vidal;Whasintong Ramírez-Montalvan;SAQUICELA GALARZA , VICTOR HUGO;Yoredy Sarmiento;Gonzalez, Patricia;Lizandro D Solano-Quinde"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/132a9c618765675be6655733aadad777"    },    "ts" : {     "type" : "literal",     "value" : "Semantic web and augmented reality for searching people, events and points of interest within of a university campus.;Semantic web and augmented reality for searching people, events and points of interest within of a university campus"    },    "names" : {     "type" : "literal",     "value" : "Juan, Contreras;Espinoza Mejía, M;Marco Andrés Tello Guerrero;Chimbo , David"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/0df076962b4dcf1cec74d64da135b8b2"    },    "ts" : {     "type" : "literal",     "value" : "Ecuadorian Geospatial Linked Data"    },    "names" : {     "type" : "literal",     "value" : "N Piedra;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO;Boris Villazón Terrazas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/d27265e1096766509c33498a5a2ab62a"    },    "ts" : {     "type" : "literal",     "value" : "Detección de Similitudes entre Contenidos Académicos de Carrera mediante Tecnologías Semánticas y Minería de Textos"    },    "names" : {     "type" : "literal",     "value" : "N Piedra;Espinoza Mejía, M;john fernando baculima;SAQUICELA GALARZA , VICTOR HUGO;Orellana , Gerardo;Marcos Eugenio Orellana Campoverde"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/1a93f02aed3828bae9c20a054067935a"    },    "ts" : {     "type" : "literal",     "value" : "Ltextthreesuperiorpez. Marco de Trabajo para la In-tegracitextthreesuperiorn de Recursos Digitales Basado en un Enfoque de Web SemÃ¡ntica;Marco de trabajo para la integracion de recursos digitales basado en un enfoque de web semantica;Framework for the integration of digital resources based-on a semantic web approach;Framework for the integration of digital resources based-on a Semantic Web approach;Marco de trabajo para la integración de recursos digitales basado en un enfoque de web semántica;Marco de Trabajo para la Integración de Recursos Digitales Basado en un Enfoque de Web Semántica;Framework for the integration of digital resources based-on a Semantic Web approach;Marco de Trabajo para la Integración de Recursos Digitales Basado en un Enfoque de Web Semántica"    },    "names" : {     "type" : "literal",     "value" : "N Piedra;Espinoza Mejía, M;Janeth Chicaiza;SAQUICELA GALARZA , VICTOR HUGO;Cadme , Elizabeth;Piedra Pullaguari, N.O.;Caro, E.T.;pricila quichimbo;J., Chizaiza;Juan Vargas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/193d628a4073c3dfb389cf474449bda5"    },    "ts" : {     "type" : "literal",     "value" : "Similarity Detection among Academic Contents through Semantic Technologies and Text Mining.;Similarity Detection among Academic Contents through Semantic Technologies and Text Mining;Similarity detection among academic contents through semantic technologies and text mining"    },    "names" : {     "type" : "literal",     "value" : "N Piedra;Espinoza Mejía, M;john fernando baculima;SAQUICELA GALARZA , VICTOR HUGO;Orellana , Gerardo;Piedra Pullaguari, N.O.;Marcos Eugenio Orellana Campoverde"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/c3a139f76144674eea1ce8f15c1f3709"    },    "ts" : {     "type" : "literal",     "value" : "Framework for the integration of digital resources based-on a Semantic Web approach,Marco de Trabajo para la Integración de Recursos Digitales Basado en un Enfoque de Web Semántica;Framework for the integration of digital resources based-on a semantic web approach | Marco de trabajo para la integración de recursos digitales basado en un enfoque de web semántica;Framework for the integration of digital resources based-on a semantic web approach [Marco de trabajo para la integración de recursos digitales basado en un enfoque de web …;Framework for the integration of digital resources based-on a semantic web approach [Marco de trabajo para la integración de recursos digitales basado en un enfo...;Framework for the integration of digital resources based-on a semantic web approach [Marco de trabajo para la integración de recursos digitales basado en un enfoque de web semántica]"    },    "names" : {     "type" : "literal",     "value" : "N Piedra;Espinoza Mejía, M;Janeth Chicaiza;SAQUICELA GALARZA , VICTOR HUGO;Cadme , Elizabeth;Caro, E.T.;pricila quichimbo;Juan Vargas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/026dcc34e11b45e95756e3320b76497a"    },    "ts" : {     "type" : "literal",     "value" : "Análisis de la influencia de las propiedades semánticas en los sistemas de recomendación;Análisis de la Influencia de las Propiedades Semánticas en los Sistemas de Recomendación"    },    "names" : {     "type" : "literal",     "value" : "Ávila, Janeth;kenneth samuel palacio baus;Xavier Riofrio;Espinoza Mejía, M;H Albán;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/64e8ac239e452f3b920e864d7d5592ce"    },    "ts" : {     "type" : "literal",     "value" : "Analysis of the influence of semantic properties in recommendation systems (Análisis de la influencia de las propiedades semanticas en los sistemas de recomendación)"    },    "names" : {     "type" : "literal",     "value" : "Ávila, Janeth;kenneth samuel palacio baus;Xavier Riofrio;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/5cccfa93d497ca3c0032f72540f9f335"    },    "ts" : {     "type" : "literal",     "value" : "Audiovisual contents recommender system: Semantic inference algorithm (Sistema de recomendación de contenidos audiovisuales: Algoritmo de inferencia semántica)"    },    "names" : {     "type" : "literal",     "value" : "Ávila, Janeth;kenneth samuel palacio baus;Xavier Riofrio;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/46d1cbee0c46451ff6540b92667c84a8"    },    "ts" : {     "type" : "literal",     "value" : "Semantic recommender systems for digital tv: From demographic stereotyping to personalized recommendations;Semantic Recommender Systems for Digital TV: From Demographic Stereotyping to Personalized Recommendations"    },    "names" : {     "type" : "literal",     "value" : "Ávila, Janeth;kenneth samuel palacio baus;Xavier Riofrio;Espinoza Mejía, M;k palaciobaus;SAQUICELA GALARZA , VICTOR HUGO;mauricio espinozamejia"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/4a481e10222cb6253896cdb79b1d530f"    },    "ts" : {     "type" : "literal",     "value" : "Decategorizing demographically stereotyped users in a semantic recommender system.;Decategorizing demographically stereotyped users in a semantic recommender system"    },    "names" : {     "type" : "literal",     "value" : "Ávila, Janeth;kenneth samuel palacio baus;Xavier Riofrio;Astudillo, D.;Espinoza Mejía, M;Astudillo , D;k palaciobaus;SAQUICELA GALARZA , VICTOR HUGO;n diana astudillo;mauricio espinozamejia;Riofrlo, X."    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/2e552749b8531026835bb3453d71656f"    },    "ts" : {     "type" : "literal",     "value" : "Sistema de recomendación de contenidos audiovisuales: Algoritmo de inferencia semántica"    },    "names" : {     "type" : "literal",     "value" : "Ávila, Janeth;DIUC;Universidad de Cuenca;kenneth samuel palacio baus;Xavier Riofrio;Espinoza Mejía, M;k palaciobaus;SAQUICELA GALARZA , VICTOR HUGO;mauricio espinozamejia"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/122cb6a2e6d993ed5df87985443ec561"    },    "ts" : {     "type" : "literal",     "value" : "REDI: A linked data-powered research networking platform;REDI: A Linked Data-Powered Research Networking Platform.;REDI: A Linked Data-powered Research Networking Platform;REDI: A Linked Data-Powered Research Networking Platform"    },    "names" : {     "type" : "literal",     "value" : "José Ortiz Segarra;Ortiz , Jose;Segarra , Jose;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO;Jose Stalin Ortiz;Boris Villazón Terrazas;boris villazonterrazas;Sumba, X.;Ortiz, J.;Jose, Segarra;j ortiz;s jose ortiz"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/286a8ec76e6bb3785a1cf332f4802127"    },    "ts" : {     "type" : "literal",     "value" : "Integration of digital repositories through federated queries using semantic technologies.;Integration of digital repositories through federated queries using semantic technologies"    },    "names" : {     "type" : "literal",     "value" : "José Ortiz Segarra;Ortiz , Jose;Segarra , Jose;Segarra, J.;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO;Jose Stalin Ortiz;Ortiz, Johana;Ortiz, J.;Jose, Segarra;j ortiz;s jose ortiz"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/3684e36de45ee67a39aee87eac810122"    },    "ts" : {     "type" : "literal",     "value" : "LOD-GF: An Integral Linked Open Data Generation Framework"    },    "names" : {     "type" : "literal",     "value" : "José Ortiz Segarra;Ortiz , Jose;Segarra , Jose;Espinoza Mejía, M;Lupercio, L.;Marco Andrés Tello Guerrero;SAQUICELA GALARZA , VICTOR HUGO;Jose Stalin Ortiz;Boris Villazón Terrazas;boris villazonterrazas;Ortiz, J.;Jose, Segarra;j ortiz;s jose ortiz"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/08724d9481317b5f90378ca95712c3a2"    },    "ts" : {     "type" : "literal",     "value" : "Authors semantic disambiguation on heterogeneous bibliographic sources.;Authors semantic disambiguation on heterogeneous bibliographic sources"    },    "names" : {     "type" : "literal",     "value" : "José Ortiz Segarra;Ortiz , Jose;Segarra , Jose;Espinoza Mejía, M;Jose Cullcay;SAQUICELA GALARZA , VICTOR HUGO;Jose Stalin Ortiz;Sumba, X.;Ortiz, J.;Jose, Segarra;j ortiz;s jose ortiz"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/1c1f9719fe0c8f7ca534f0809241d7bb"    },    "ts" : {     "type" : "literal",     "value" : "On the implementation of a laboratory of digital television according to the ISDB-Tb Standard;On the implementation of a Laboratory of Digital Television according to the ISDB-Tb standard"    },    "names" : {     "type" : "literal",     "value" : "Villa, C;DIUC;Universidad de Cuenca;kenneth samuel palacio baus;Medina, J;Astudillo, D.;Jose Ramon, Medina;Espinoza Mejía, M;Jose, Medina;Astudillo , D;Jose R., Medina;H Albán;k palaciobaus;SAQUICELA GALARZA , VICTOR HUGO;n diana astudillo;mauricio espinozamejia;Christian Villa"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/c05eb17ba71c423a192fa0a55ed69e07"    },    "ts" : {     "type" : "literal",     "value" : "Explotación de información en el dominio geo-hídrico ecuatoriano utilizando tecnología semántica"    },    "names" : {     "type" : "literal",     "value" : "DIUC;Universidad de Cuenca;Espinoza Mejía, M;john fernando baculima;Lupercio, L.;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/6c65acb26e188a55f0423819e15c4f5f"    },    "ts" : {     "type" : "literal",     "value" : "Mejorando la visibilidad de sitios Web usando tecnología semántica;Mejorando la visibilidad de sitios web usando tecnología semántica"    },    "names" : {     "type" : "literal",     "value" : "DIUC;Universidad de Cuenca;Lenin Montenegro;Espinoza Mejía, M;OCHOA ROBLES ,  MARIA VERONICA;mauricio espinozamejia"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/b62c0da721236f71a671e13456d74132"    },    "ts" : {     "type" : "literal",     "value" : "Current challenges of interactive digital television"    },    "names" : {     "type" : "literal",     "value" : "kenneth samuel palacio baus;Espinoza Mejía, M;Lissette Muñoz;Magali, Mejia-Pesantez"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/b1a7431bb1b434b08c224ad92a4edd78"    },    "ts" : {     "type" : "literal",     "value" : "Identifying Common Research Areas: A Study Case"    },    "names" : {     "type" : "literal",     "value" : "kenneth samuel palacio baus;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO;Jorge Bermeo;Boris Villazón Terrazas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/f84da0f5fa6c647b75911742a4fb2475"    },    "ts" : {     "type" : "literal",     "value" : "Tele-vision preferences extraction from social networks profiles-(extracción de preferencias televisivas desde los perfiles de redes sociales);Television preferences extraction from social networks profiles-(extracción de preferencias televisivas desde los perfiles de redes sociales)"    },    "names" : {     "type" : "literal",     "value" : "kenneth samuel palacio baus;Espinoza Mejía, M;H Albán;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/9ddc143ea94142f81952de59466efd85"    },    "ts" : {     "type" : "literal",     "value" : "Leveraging social data with s emantic technology"    },    "names" : {     "type" : "literal",     "value" : "kenneth samuel palacio baus;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO;Nuria Garcıa Santa;Boris Villazón Terrazas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/2c6e76576e720ec90320ba9a2554fb50"    },    "ts" : {     "type" : "literal",     "value" : "Tv program recommender using user authentication on middleware ginga;TV program recommender using user authentication on middleware ginga;TV program recommender using user authentication on middleware Ginga"    },    "names" : {     "type" : "literal",     "value" : "kenneth samuel palacio baus;Espinoza Mejía, M;Marco Andrés Tello Guerrero;k palaciobaus;SAQUICELA GALARZA , VICTOR HUGO;Jorge Crespo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/f5951cef85bfe92eaf28f5367b8d25cd"    },    "ts" : {     "type" : "literal",     "value" : "Extracción de preferencias televisivas desde los perfiles de redes sociales;Extracción de Preferencias Televisivas desde los Perfiles de Redes Sociales"    },    "names" : {     "type" : "literal",     "value" : "kenneth samuel palacio baus;Espinoza Mejía, M;H Albán;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/46bdc781ab69a4640b9bdaaecd4d51ec"    },    "ts" : {     "type" : "literal",     "value" : "Enriching electronic program guides using semantic technologies and external resources;Enriching Electronic Program Guides using semantic technologies and external resources.;Enriching Electronic Program Guides using semantic technologies and external resources"    },    "names" : {     "type" : "literal",     "value" : "kenneth samuel palacio baus;Espinoza Mejía, M;H Albán;SAQUICELA GALARZA , VICTOR HUGO;mauricio espinozamejia"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/6712da244a6fb54fabdc4cb1895e9815"    },    "ts" : {     "type" : "literal",     "value" : "Towards the Creation of a Semantic Repository of iStar-Based Context Models and the DHARMA Method [Hacia la Creación de un Repositorio Semántico de Modelos de Contexto Basados …;Towards the Creation of a Semantic Repository of iStar-Based Context Models and the DHARMA Method | Hacia la Creación de un Repositorio Semántico de Modelos de Contexto Basados en i* y el método DHARMA;Towards the creation of a semantic repository of iStar-based context models and the DHARMA method/Hacia la creacion de un repositorio semantico de modelos de contexto basados …;Towards the creation of a semantic repository of iStar-based context models and the DHARMA method/Hacia la creacion de un repositorio semantico de modelos de...;Towards the Creation of a Semantic Repository of iStar-Based Context Models and the DHARMA Method [Hacia la Creación de un Repositorio Semántico de Modelos de Contexto Basados en i* y el método DHARMA]"    },    "names" : {     "type" : "literal",     "value" : "CARVALLO VEGA ,  JUAN PABLO;Abad , Karina;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/3bdb0c210b5731d093a7c962cac583f8"    },    "ts" : {     "type" : "literal",     "value" : "Hacia la Creación de un Repositorio Semántico de Modelos de Contexto Basados en i∗ y el método DHARMA;Towards the creation of a semantic repository of iStar-based context models and the DHARMA method;Towards the creation of a semantic repository of istar-based context models;Towards the Creation of a Semantic Repository of iStar-Based Context Models and the DHARMA Method;Hacia la creación de un repositorio semántico de modelos de contexto basados en i* y el método DHARMA;Towards the Creation of a Semantic Repository of iStar-Based Context Models;Hacia la Creación de un Repositorio Semántico de Modelos de Contexto Basados en i* y el método DHARMA;Towards the creation of a semantic repository of iStar-based context models;Towards the Creation of a Semantic Repository of iStar-Based Context Models and the DHARMA Method;Hacia la Creación de un Repositorio Semántico de Modelos de Contexto Basados en i* y el método DHARMA"    },    "names" : {     "type" : "literal",     "value" : "CARVALLO VEGA ,  JUAN PABLO;Abad , Karina;Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/7ac22b9f536313c82e57c9e17c4a8384"    },    "ts" : {     "type" : "literal",     "value" : "Ontología DHARMA para la construcción de arquitectura de sistemas empresariales"    },    "names" : {     "type" : "literal",     "value" : "CARVALLO VEGA ,  JUAN PABLO;Abad , Karina;Espinoza Mejía, M;Perez , Wilson;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "http://api.elsevier.com/content/abstract/scopus_id/45449083232"    },    "ts" : {     "type" : "literal",     "value" : "Labeltranslator - Automatically localizing an ontology;LabelTranslator - A Tool to Automatically Localize an Ontology;LabelTranslator - A tool to automatically localize an ontology"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/7f92757cebfc5fd75cad35558c3e523f"    },    "ts" : {     "type" : "literal",     "value" : "Discovering Web services using semantic keywords;Discovering web services using semantic keywords;Discovering Web Services Using Semantic Keywords"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Mena , Eduardo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/d01438049447dddd7cf2147b77554abc"    },    "ts" : {     "type" : "literal",     "value" : "Revision and extension of the NeOn Methodology for building contextualized ontology networks;D5. 4.3. Revision and Extension of the NeOn Methodology for Building Contextualized Ontology Networks;D5.4.2. Revision and Extension of the NeOn Methodology for Building Contextualized Ontology Networks;D5. 4.2 Revision and Extension of the NeOn Methodology for Building Contextualized Ontology Networks"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Igor Mozetic;Jose Angel Ramos UPM;E. and D’Aquin;R. and Poveda;Chan Le Duc;R Palma;B. Zablith;Holger Lewen;I. and Palma;F. and Dzbor;Jérôme Euzenat;A. and Lewen;F Zablith;H. and Mozetic;Mari Carmen Suarez-Figueroa;E Blomqvist;M;Maria Poveda;M.C and Blomqvist;M. and Villazón-Terrazas;Martin Dzbor;Mathieu d’Aquin;Boris Villazón Terrazas;M. and Sini"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/bda64c78f112eaa0ee865f9634a0de5f"    },    "ts" : {     "type" : "literal",     "value" : "Estudio y seleccion de una arquitectura orientada a servicios (SOA) que permita la integracion de sistemas informaticos legados"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Felipe E Cisneros;Jaime Eduardo Veintimilla"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/91907b0c3a87a5c0a49aad40912a95ff"    },    "ts" : {     "type" : "literal",     "value" : "El ciclo de vida de un servicio Web compuesto: virtudes y carencias de las soluciones actuales"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Pedro Álvarez"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/a3d1237ed64fe5ccd5e36922da33f98e"    },    "ts" : {     "type" : "literal",     "value" : "Estado del Arte en torno a la Composición de Servicios Web"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Pedro Álvarez"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/2deeb140f029b3652007b36eee1b3dbe"    },    "ts" : {     "type" : "literal",     "value" : "D5. 6.2 Experimentation and Evaluation of the NeOn Methodology"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;R Palma;Holger Lewen;Mari Carmen Suarez-Figueroa;E Blomqvist;Martin Dzbor"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/4b6f12815ae4845c62abea6ce7407059"    },    "ts" : {     "type" : "literal",     "value" : "The application of the modular multilevel matrix converter in high-power wind turbines"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;patrick wheeler;Andres Mora;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/9b4191ed395c4096f035c903cb3cc3b3"    },    "ts" : {     "type" : "literal",     "value" : "Modelo para Almacenar y Recuperar Métricas de Software"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Enrique Luna Ramírez;Francisco J Álvarez Rdz;Humberto Ambriz;Antonio Nungaray"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/f864b53db2ee9a38729ae80f69017610"    },    "ts" : {     "type" : "literal",     "value" : "Improved control strategy of the modular multilevel converter for high power drive applications in low frequency operation"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Espina , Enrique;Andres Mora;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/f9a36f515b08e857a6c41e8e3bfb49bc"    },    "ts" : {     "type" : "literal",     "value" : "An Enhanced $dq$-Based Vector Control System for Modular Multilevel Converters Feeding Variable-Speed Drives"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Clare , Jon C;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/88fd7801dd39664c812f41a0071901d5"    },    "ts" : {     "type" : "literal",     "value" : "Una ontología para representar el contexto en sistemas de computación académicos universitarios"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Lourdes Eugenia Illescas Peña;Lenin Xavier Erazo Garzón"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/ed2fad653f215e4b85fb262f7bf81d25"    },    "ts" : {     "type" : "literal",     "value" : "Model Predictive Control of Modular Multilevel Matrix Converter"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Andres Mora;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/cdef5f15ec9b90ed035a66cec6093286"    },    "ts" : {     "type" : "literal",     "value" : "D5. 6.2 Experimentation with parts of NeOn methodology"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Holger Lewen;Mari Carmen Suarez-Figueroa;E Blomqvist;Martin Dzbor"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/132f8027c09659919961673dc1bd4c9c"    },    "ts" : {     "type" : "literal",     "value" : "Sistema de manejo y actualización de catastros para proyectos de riego"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Lupercio, L."    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/72981f3f0a9f79eddb92e536a0bf5c7f"    },    "ts" : {     "type" : "literal",     "value" : "Reduciendo la sobrecarga de información en usuarios de televisión digital"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO;Boris Villazón Terrazas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/0301d572accb265b94f6a525e54657cc"    },    "ts" : {     "type" : "literal",     "value" : "Semantic integration of Ecuadorian geospatial data in the context of hydrology domain"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Lupercio, L.;Vilches,Luis M;Eduardo Tacuri;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/fb8998b1810e717926f5952bbc958487"    },    "ts" : {     "type" : "literal",     "value" : "Ontology Localization."    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda;G Auguado de Cea"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/5c1ea732d455c0be8ccca5b73c4499c6"    },    "ts" : {     "type" : "literal",     "value" : "Neon deliverable d5. 4.1. Neon methodology for building contextualized ontology networks"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;A García;Mari Carmen Suarez-Figueroa;M Sabou;E Montiel-Ponsoda;Boris Villazón Terrazas;K Dellschaft;Zehn Yufei;Mariano Fernández-López;G Auguado de Cea"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/d2255f3cd165fa054a01e27856b083cf"    },    "ts" : {     "type" : "literal",     "value" : "Ontology Localization"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/0a6076cf9aee829f1cd1e77ebf0d518f"    },    "ts" : {     "type" : "literal",     "value" : "NeOn D5. 4.1"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;A García;C Buil;G Herrero;Mari Carmen Suarez-Figueroa;M Sabou;E Montiel-Ponsoda;Boris Villazón Terrazas;K Dellschaft;Zehn Yufei;Mariano Fernández-López;G Auguado de Cea"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/f2fa294c0734473622c29092118a6534"    },    "ts" : {     "type" : "literal",     "value" : "Ontology localization"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda;G Auguado de Cea"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/07d23bc1a3da06d5a8be3652c44a8c24"    },    "ts" : {     "type" : "literal",     "value" : "Ontology localization"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;elena montielponsoda;asuncion gomezperez"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/91f7e689c743b37630f3e9613947128b"    },    "ts" : {     "type" : "literal",     "value" : "Ontology localization."    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/1dd988ec086523c19483f1fc7944dd89"    },    "ts" : {     "type" : "literal",     "value" : "Ontology localization"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/af80a97eaaf2611e77d0e2b487ed0226"    },    "ts" : {     "type" : "literal",     "value" : "Ontology Localization."    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda;G Auguado de Cea"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/2f005d4d0d5856681e4f3ee8ac975be1"    },    "ts" : {     "type" : "literal",     "value" : "Control of modular multilevel cascade converters for offshore wind energy generation and transmission"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;felipe donoso;patrick wheeler;Andres Mora;felix rojas;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/70406b45233ba661dfe806b6b4f3b4b1"    },    "ts" : {     "type" : "literal",     "value" : "Control strategies for modular multilevel converters driving cage machines"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Espina , Enrique;Hackl , Christoph M;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/3ce1e3726580b7504c85710c46c18da7"    },    "ts" : {     "type" : "literal",     "value" : "An Integrated Converter and Machine Control System for MMC-Based High-Power Drives."    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Soto Sanchez , Diego E;Clare , Jon C;Espina , Enrique;Hackl , Christoph M;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/f83bf8a27b4cb77377bcf6a53688c0d8"    },    "ts" : {     "type" : "literal",     "value" : "Closed loop vector control of the modular multilevel matrix converter for equal input-output operating frequencies"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;patrick wheeler;Andres Mora;felix rojas;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/5d20d533b802f1ceaba6556bb96b24b4"    },    "ts" : {     "type" : "literal",     "value" : "Ontology localization."    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/76b3848e2b62395296d26cd86e07ceb5"    },    "ts" : {     "type" : "literal",     "value" : "Control of Wind Energy Conversion Systems Based on the Modular Multilevel Matrix Converter.;Control of Wind Energy Conversion Systems Based on the Modular Multilevel Matrix Converter"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Clare , Jon C;patrick wheeler;Andres Mora;felix rojas;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/2296e36c0e0e6e7e480053eea7e53d73"    },    "ts" : {     "type" : "literal",     "value" : "An Enhanced dq-Based Vector Control System for Modular Multilevel Converters Feeding Variable-Speed Drives."    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Clare , Jon C;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/12bed587ae223f3b693d82fcb71bf0fc"    },    "ts" : {     "type" : "literal",     "value" : "Vector Control of a Modular Multilevel Matrix Converter Operating Over the Full Output-Frequency Range;Vector Control of a Modular Multilevel Matrix Converter Operating in the Full Output-Frequency Range"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Clare , Jon C;patrick wheeler;Hackl , Christoph M;felix rojas;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/06d1ae354d4297680c826b8b22e0ed0b"    },    "ts" : {     "type" : "literal",     "value" : "Discovering the Semantics of User Keywords.;Discovering the Semantics of User Keywords"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Gracia , Jorge;Lado,Raquel Trillo;Mena , Eduardo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/1d4abb9bf927940b8d68bf591ae0150f"    },    "ts" : {     "type" : "literal",     "value" : "Discovering the Semantics of Keywords: An Ontology-based Approach.;Discovering the Semantics of Keywords: An Ontology-based Approach ∗"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Gracia , Jorge;Lado,Raquel Trillo;Mena , Eduardo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/a70a9862ff9d653b6f638692fb3eb286"    },    "ts" : {     "type" : "literal",     "value" : "Ontology localization"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda;G Auguado de Cea"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/3e89021f14a68159c6f5af23c97e6d54"    },    "ts" : {     "type" : "literal",     "value" : "An ontology to represent geographic information in the Ecuadorian hydric domain,Una ontología para representar la información geográfica en el dominio hídrico ecuatoriano;An ontology to represent geographic information in the Ecuadorian hydric domain;Una ontologıa para representar la información geográfica en el dominio hıdrico ecuatoriano;Una ontología para representar la información geográfica en el dominio hídrico ecuatoriano;Una ontología para representar la información geográfica en el dominio hídrico Ecuatoriano"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;john fernando baculima;Lupercio, L.;SAQUICELA GALARZA , VICTOR HUGO;Jhon Fernando Baculima Cumbe"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/78b818678844bd491ab06a9a042b827e"    },    "ts" : {     "type" : "literal",     "value" : "Ontology localization"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;E Montiel-Ponsoda"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/011b22e80075f587f40ebab9dca18294"    },    "ts" : {     "type" : "literal",     "value" : "Una Ontología para Representar la Información Geográfica en el Dominio Hídrico Ecuatoriano (An Ontology to Represent Geographical Information in the Ecuadorian Hydric Domain)."    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;john fernando baculima;Lupercio, L.;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/96cb92289d39c9eb3092a1ba93a04f70"    },    "ts" : {     "type" : "literal",     "value" : "Enriching an Ontology with Multilingual Information.;Enriching an ontology with multilingual information"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;asuncion gomezperez;Mena , Eduardo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/58b1a475d226361a511732156b34d70a"    },    "ts" : {     "type" : "literal",     "value" : "A note on ontology localization.;A note on ontology localization"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;elena montielponsoda;asuncion gomezperez;E Montiel-Ponsoda;Cimiano,Philipp;Paul Buitelaar"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/af49e45513641f6ca29f2be3c6e0188a"    },    "ts" : {     "type" : "literal",     "value" : "Geolinked data and inspire through an application case;GeoLinked data and INSPIRE through an application case.;GeoLinked data and INSPIRE through an application case"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Vilches,Luis M;asuncion gomezperez;SAQUICELA GALARZA , VICTOR HUGO;De León, A.;Boris Villazón Terrazas;boris villazonterrazas;Óscar, Corcho"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/1e3051c8843a6833f68b721acef1a9c3"    },    "ts" : {     "type" : "literal",     "value" : "Repositorio Semántico de Investigadores del Ecuador"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;john fernando baculima;Sumba, Freddy;Jose Cullcay;SAQUICELA GALARZA , VICTOR HUGO;José Luis Cullcay"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/87f74c5c7da4e9d7ca1cae97ae14fe83"    },    "ts" : {     "type" : "literal",     "value" : "Detecting similar areas of knowledge using semantic and data mining technologies;Detecting Similar Areas of Knowledge Using Semantic and Data Mining Technologies.;Detecting Similar Areas of Knowledge Using Semantic and Data Mining Technologies"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;john fernando baculima;Marco Andrés Tello Guerrero;Sumba, Freddy;SAQUICELA GALARZA , VICTOR HUGO;Sumba, X."    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/193ed48120f903e7bfee2a07bf1c45cf"    },    "ts" : {     "type" : "literal",     "value" : "Modelando los Hábitos de Consumo Televisivo usando Tecnologıa Semantica;Modelando los hábitos de consumo televisivo usando tecnología semántica"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;SAQUICELA GALARZA , VICTOR HUGO"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/b4da5f678e6cd1781b60d6b41b1f2ca7"    },    "ts" : {     "type" : "literal",     "value" : "Combining statistical and semantic approaches to the translation of ontologies and taxonomies.;Combining statistical and semantic approaches to the translation of ontologies and taxonomies"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;guadalupe aguadodecea;elena montielponsoda;E Montiel-Ponsoda;Cimiano,Philipp;John McCrae;G Auguado de Cea"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/d02ca815256b76c433ec552a2c6d3a07"    },    "ts" : {     "type" : "literal",     "value" : "Querying the web: A multiontology disambiguation method;Querying the web: a multiontology disambiguation method.;Querying the web: a multiontology disambiguation method"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Gracia , Jorge;Lado,Raquel Trillo;Mena , Eduardo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/df1da7fdefde57fa1ba84e568c14b419"    },    "ts" : {     "type" : "literal",     "value" : "Model-Predictive-Control-Based Capacitor Voltage Balancing Strategies for Modular Multilevel Converters.;Model-Predictive-Control-Based Capacitor Voltage Balancing Strategies for Modular Multilevel Converters"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Cardenas , Alejandro Angulo;Urrutia , Matias;Andres Mora;pablo lezana;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/bda807818ae7845be63bfd6f01a537ce"    },    "ts" : {     "type" : "literal",     "value" : "Active power oscillation elimination in 4-leg grid-connected converters under unbalanced network conditions.;Active power oscillation elimination in 4-leg grid-connected converters under unbalanced network conditions"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Andres Mora;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/93b7eee8c361ad8945183bfefea639d5"    },    "ts" : {     "type" : "literal",     "value" : "Modelling and control of the modular multilevel converter in back to back configuration for high power induction machine drives.;Modelling and control of the modular multilevel converter in back to back configuration for high power induction machine drives"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;Soto Sanchez , Diego E;Andres Mora;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/605096d8a27925f615932cce6f6b0659"    },    "ts" : {     "type" : "literal",     "value" : "Discovering and Merging Keyword Senses using Ontology Matching.;Discovering and merging keyword senses using ontology matching"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Gracia , Jorge;Lado,Raquel Trillo;Mena , Eduardo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/d69195523d17bdecbf4ba1758ee60755"    },    "ts" : {     "type" : "literal",     "value" : "Modelling and control of the Modular Multilevel Matrix Converter and its application to Wind Energy Conversion Systems.;Modelling and control of the Modular Multilevel Matrix Converter and its application to Wind Energy Conversion Systems"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Diaz , Matias;patrick wheeler;Andres Mora;roberto cardenas"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/28af15fbd7cd0f3c380df6d9263c87f6"    },    "ts" : {     "type" : "literal",     "value" : "Finding teleconnections from decomposed rainfall signals using dynamic harmonic regressions: a Tropical Andean case study"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;Esteban, Samaniego;Daniel Mendoza;CAMPOZANO PARRA ,  LENIN VLADIMIR;Mora, D. E."    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/a36b0fd48959573f39bc3958901c1b72"    },    "ts" : {     "type" : "literal",     "value" : "Labeltranslator - Automatically localizing an ontology;LabelTranslator - A tool to automatically localize an ontology;LabelTranslator-a tool to automatically localize an ontology;LabelTranslator - A Tool to Automatically Localize an Ontology.;LabelTranslator - a tool to automatically localize an ontology"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;asuncion gomezperez;Mena , Eduardo"    }   }, {    "pub" : {     "type" : "uri",     "value" : "https://redi.cedia.edu.ec/resource/publication/96b8c88df91c43bdfd2ac6cdf6d6aa08"    },    "ts" : {     "type" : "literal",     "value" : "Multilingual and localization support for ontologies;Multilingual and Localization Support for Ontologies.;Multilingual and Localization Support for Ontologies"    },    "names" : {     "type" : "literal",     "value" : "Espinoza Mejía, M;W Peters;elena montielponsoda;asuncion gomezperez;E Montiel-Ponsoda;M. and Sini;G Auguado de Cea"    }   } ]  } }';
     var apj = JSON.parse(authorprofile);
     var apn = JSON.parse(authorname);
     var apub =  JSON.parse(publication); 
     //console.log (apj);
     var dataSet = [];
     _.each(apj.results.bindings, function (a){
            var name = a.names.value;
            var uri = a.object.value;
            var k = "";
        //  console.log (a.names.value+" "+a.object.value);
          if (a.lbls) {
        //     console.log (a.lbls.value);
             k = a.lbls.value.toLowerCase().substring(0,100);
          }

          var d = [uri , name , k ];
          dataSet.push (d);
     });

         var dataSetn = [];
          _.each(apn.results.bindings, function (a){
          //  console.log (a);
            var name = [a.name.value];
            dataSetn.push (name);
     });
          // console.log (dataSetn);

         var datamail = [["mauricio.espi@yahoo.xxx"],["Mauricio.espinoza@ucuenca.edu.ec"]];  
         var dataSetp = [];
      _.each(apub.results.bindings, function (a){
          var p = [ a.ts.value , a.names.value];
          dataSetp.push (p);
            
            //var name = [a.name.value];
            //dataSetn.push (name);
     });
       // console.log (dataSetp);
         var table;// =  $('#profileval1').DataTable( {});
         var table2;
         var table3;
         var table4;

               /*  var senddata = {"objeto":'{ intro : 10 }' };
            saveprofile.querySrv({data: senddata }, function (data) { 
               console.log ("STATUS");
               console.log (data);
         });*/
         
     

              waitingDialog.show("Cargando información, espere por favor");

         profileval.query({'id': author , 'orcid' : orcid  }, function (data) { 
           waitingDialog.hide();
            var alldata  = [];
         //    console.log (data.profiles.data);
            var tabla =  rendertable ( data.profiles.data );
            var tabla2 =  rendertable2 ( data.names.data );
            var tabla3 =  rendertable3 ( data.emails.data );
            var tabla4 =  rendertable4 ( data.publications.data );
            var datasave = [];
           // rendertable2 (dataSet)
            alldata.push ({"sec" : "secone" , "nametable": "profiles", "table": tabla , "data" : datasave }, {"sec" : "sectwo", "nametable": "names", "table": tabla2 , "data" : datasave  } , { "sec" : "secthree" , "nametable": "emails", "table": tabla3 ,  "data" : datasave  } ,{ "sec" : "secfour" ,"nametable": "publications", "table": tabla4 , "data" : datasave  });
             pagination (alldata);
         } , function (some){
          //  console.log ("Que llega?");
           console.log (some);
           alert ("Problemas al cargar los datos");
              waitingDialog.hide();
         });

       
            saveprof = function () {
            console.log ("Almacenando datos");
            saveprofile.querySrv({'data': JSON.stringify(dataforsave) , 'id' : orcid , 'uri' : author , 'atk' : atk }, function (data) { 
               console.log ("STATUS");
             //  console.log (data);
               alert ("Datos almacenados");
                $('#exampleModal').modal('hide');
               $window.location.hash = '/author/profile/' + author;
              
         } , function (some){
           // console.log ("Que llega?");
           console.log (some);
              waitingDialog.hide();
         });

  }

             checkall = function (param) {
               console.log (param);
         if ($("input:checkbox#maincheckbox."+param).prop("checked")){
       $("input:checkbox."+param+":not(#maincheckbox)").prop("checked", true);

        } else {
       $("input:checkbox."+param+":not(#maincheckbox)").prop("checked", false);
    
        }

     }

     function rendertable ( dataSet ){
      table =  $('#profileval1').DataTable( {
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info":     true,
        "searching":     false ,
        "pageLength": 5 ,
           data: dataSet,
        columns: [
            { title:  '<input onclick="checkall(\'profiles\')" id ="maincheckbox" class="profiles" type="checkbox" name="selection" >' },
            { data: "uri" },
            { data: "name" },
            { data: "subject" },
            ] ,  columnDefs: [  
                 {
                "render": function(data, type, row) {
                    return data ;
                },
                targets: 1 },
                { "render": function (data, type, row) {
                  return data;
                } , targets: 2 },
                 { "render": function (data, type, row) {
                  return   data ? data.toLowerCase().substring(0,100)+"..." : ""  ;
                }, targets: 3 },
                {
                "render": function(data, type, row) {
                  
                     var status = "";
                     if (row["status"]){
                      status = "checked";
                     }
                      return '<input id="' + row["uri"] + '" class="profiles" type="checkbox" name="selection" value="' + row["uri"] + '"  '+status+'>';
                },
                targets: 0 } 
              

            ]
        }); 
     return table;
       }

          function rendertable2 (dataSet){
     table2 =  $('#profileval2').DataTable( {
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info":     true,
        "searching":     false ,
        "pageLength": 5 ,
           data: dataSet,
        columns: [
            { title: '<input onclick="checkall(\'names\')" id ="maincheckbox" class="names" type="checkbox" name="selection" >'  },
            { data: "name" },
            { data: "other" }
            ] ,  columnDefs: [  
                 {
                "render": function(data, type, row) {
                 
                    return data ;
                },
                targets: 1 },
                { "render": function (data, type, row) {
                  return  data ;
                } , targets: 2 },
                {
                "render": function(data, type, row) {
                    var status = "";
                     if (row["status"]){
                      status = "checked";
                     }
                    return '<input id="' + row["name"] + '" class="names" type="checkbox" name="selection" value="' + row["name"] + '" '+status+'>';
                },
                targets: 0 } 
              

            ]
        }); 
        return table2;
      }


           function rendertable3 (dataSet){
     table3 =  $('#profileval3').DataTable( {
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info":     true,
        "searching":     false ,
        "pageLength": 5 ,
           data: dataSet,
        columns: [
            { title: '<input onclick="checkall(\'emails\')" id ="maincheckbox" class="emails" type="checkbox" name="selection" >'  },
            { data: "mail" }
            ] ,  columnDefs: [  
                 {
                "render": function(data, type, row) {
               
                    return data ;
                },
                targets: 1 },
                {
                "render": function(data, type, row) {
                    var status = "";
                     if (row["status"]){
                      status = "checked";
                     }
                    return '<input id="' + row["mail"] + '" class="emails"  type="checkbox" name="selection" value="' + row["mail"] + '" '+status+'>';
                },
                targets: 0 } 
              

            ]
        }); 
          return table3; }

         function rendertable4 (dataSet) {
        table4 =  $('#profileval4').DataTable( {
        "dom": "lfrti",
        "lengthChange": false,
        "ordering": false,
        "info":     true,
        "searching":     false ,
        "pageLength": 5 ,
           data: dataSet,
        columns: [
            { title: '<input onclick="checkall(\'publications\')" id ="maincheckbox" class="publications" type="checkbox" name="selection" >' },
            { data: "title" } ,
            { data: "authors" }
            ] ,  columnDefs: [  
                 {
                "render": function(data, type, row) {
                 
                    return data.split(";")[0] ;
                },
                targets: 1 },
                 {
                "render": function(data, type, row) {
                 
                    return data.toUpperCase() ;
                },
                targets: 2 },
                  {
                "render": function(data, type, row) {
                    var status = "";
                     if (row["status"]){
                      status = "checked";
                     }
                    return '<input id="' + row["uri"] + '" class="publications"  type="checkbox" name="selection" value="' + row["uri"] + '" '+status+'>';
                },
                targets: 0 } 
              

            ]
        });
             return table4;
         }

          var i = 0; 
          var activetable ;
          var dataforsave = { 'profiles' : [] , 'names' : [] , 'emails' : [] , 'publications' : []  };
       function pagination (alldata) {
      
    //  var alldata = [{ "sec": "secone", "table": table } , { "sec": "sectwo", "table": table2 } , { "sec": "secthree", "table": table3 } , { "sec": "secfour", "table": table4 }]; 
      activetable = alldata[0].table;
      i = 0; 
    $('#nextButtonTable').on( 'click', function () {
      //console.log (table.page.info());
    //  console.log (table.page());
     $("input:checkbox#maincheckbox."+alldata[i].nametable).prop('checked',false);
       dataforsave[alldata[i].nametable].push( savepag (alldata[i]));
      // console.log (dataforsave);
    if ((activetable.page.info().page+1) < activetable.page.info().pages ){
     activetable.page( 'next' ).draw( 'page' );
   }else 
   {   
      i = showsection (i , true , alldata);
  
   }
    //  console.log (table.page.info());
      // console.log (table.page());
     });

    $('#prevButtonTable').on( 'click', function () {
     //    console.log (page());
    
    //console.log (dataforsave);
    if ( activetable.page.info().page == 0  ){
     i = showsection (i , false , alldata);

    } else {
    activetable.page( 'previous' ).draw('page');
    
     }
       dataforsave[alldata[i].nametable].pop();
   // console.log (alldata[i].nametable);

    }); }

    function showsection (i , next  ,alldata) {
         // console.log (i+next);
         if (next && i < alldata.length-1 ){
         
           i++;
         var sec = alldata[i].sec;
         $(".sectionval").removeClass("active");
         $('#'+sec).addClass("active");
         activetable = alldata[i].table ;
      //   console.log ($('#'+sec));
        } else if (next && i == alldata.length -1){
            //  alert ("Gracias por participar. En unos dias sus datos seran actualizados");
            console.log ("FINISH");
          //  console.log (dataforsave);
            $('#exampleModal').modal();
               // var senddata = {"objeto":dataforsave };
          
         } else if (i>0) {
          i--;
          var sec = alldata[i].sec;
         $(".sectionval").removeClass("active");
         $('#'+sec).addClass("active");
         activetable = alldata[i].table ; 
           
        }
        return i;
    }
   function savepag ( tables){
     var proftable = $("input:checkbox."+tables.nametable);
       return recoverstatus ( proftable  );
   }



 /*   function savedata () {
      var proftable = $("input:checkbox.profiles");
       var profdata = []; 
      console.log ( recoverstatus ( proftable  ));


   //  console.log ();

  }*/


  function recoverstatus ( datatable  ) {
        var data = [];
     _.each(datatable , function (a){
           data.push ({ 'id' : a.value , 'status': a.checked });
        });
     return data;
  }
}]);

      /*function saveprof (){
           saveprofi();
         alert ("Save external");
          } */