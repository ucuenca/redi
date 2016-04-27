wkhomeControllers.controller('exportController', ['$scope', function($scope) {
    $scope.exportData = function (type, data) {
        
        var appType;
        if (data) {
            switch (type) {
                case 'pdf':
                    appType = "application/x-pdf";
                    
                    var doc = new jsPDF('p','in','a4')
                    , size = 11
                    , font = ['Times','Roman']
                    , lines
                    , margin = 0.8 // inches on a 8.5 x 11 inch sheet.
                    , verticalOffset = margin + 0.5
                    ;
                    
                    doc.setFontSize(size + 1);
                    
                    doc.setFontType("bold");
                    
                    doc.text(0.5, 0.5, 'Investigador: ' + data[0]["foaf:name"]);
                    doc.text(0.5, 1, 'Numero de Publicaciones: ' + data[0]["foaf:publications"].length);
                    doc.text(3, 1.5, 'Listado de Publicaciones: ');
                    
                    doc.setFontSize(size).setFont(font[0], font[1]);
                    
                    for	(var i = 1; i < data.length; i++) {
                        
                        verticalOffset += 0.5;
                        //Adding Title
                        lines = doc.splitTextToSize("" + i + ". " + data[i]["dct:title"], 7.00);
                                        
                        var nextOffset = verticalOffset + (lines.length + 0.5) * size / 72;
                        
                        if (nextOffset > 11.10) {
                            doc.addPage('a4');
                            verticalOffset = margin;
                            nextOffset = verticalOffset + (lines.length + 0.5) * size / 72;
                        }
                        
                        doc.setFontType("bold");
                        doc.text(0.5, verticalOffset + size / 72, lines);
                        verticalOffset = nextOffset;
                        
                        //Adding Abstract
                        doc.setFontType("normal");
                        if (data[i]["bibo:abstract"]) {
                            lines = doc.splitTextToSize("ABSTRACT: " + data[i]["bibo:abstract"], 7.30);

                            nextOffset = verticalOffset + (lines.length + 0.5) * size / 65;

                            if (nextOffset > 11.10) {
                                verticalOffset = margin;
                                doc.addPage('a4');
                                nextOffset = verticalOffset + (lines.length + 0.5) * size / 65;
                            }
                            
                            doc.text(0.5, verticalOffset + size / 65, lines);
                            verticalOffset = nextOffset;
                        }
                        
                        var authors = data[i]["uc:contributor"];
                        if (authors) {
                            var contributor = "";
                            if (authors && (authors.constructor === Array || authors instanceof Array)) {
                                for (var j = 0; j < authors.length; j++) {
                                    contributor = (contributor ? contributor + "; " : " ") + authors[j];
                                }
                            } else {
                                contributor = data[i]["uc:contributor"];
                            }
                            
                            lines = doc.splitTextToSize("AUTOR(ES):" + contributor, 7.30);

                            nextOffset = verticalOffset + (lines.length + 0.5) * size / 72;

                            if (nextOffset > 11.10) {
                                doc.addPage('a4');
                                verticalOffset = margin;
                                nextOffset = verticalOffset + (lines.length + 0.5) * size / 72;
                            }

                            doc.text(0.5, verticalOffset + size / 72, lines);
                            verticalOffset = nextOffset;
                        }
                    }
                    
                    doc.save('Reporte.pdf');
                    
                    break;
                case 'xls':
                    appType = "application/vnd.ms-excel";
                    var blob = new Blob([document.getElementById('exportable').innerHTML], {
                        type: appType
                    });
                    saveAs(blob, "Report." + type);
                    break;
            }
        }
    };



}]);


