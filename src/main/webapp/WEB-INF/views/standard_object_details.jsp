<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="java.util.ArrayList"%>


            <!-- Exportable Table -->
            <div id="vr" class="row clearfix">
                <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                    <div class="card">
                        <div class="header">
                            <h2 style="color:green">
                            
                              <%=request.getAttribute("name").toString().toUpperCase() %> OBJECT VALIDATION RULE
                            </h2>
                            
                            <h4>Total Validation Rule=<%=request.getAttribute("vsize") %></h4>
                            
                            <ul class="header-dropdown m-r--5">
                                <li class="dropdown">
                                    <a href="javascript:void(0);" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                                        <i class="material-icons">more_vert</i>
                                    </a>
                                    <ul class="dropdown-menu pull-right">
                                        <li><a href="javascript:void(0);">Action</a></li>
                                        <li><a href="javascript:void(0);">Another action</a></li>
                                        <li><a href="javascript:void(0);">Something else here</a></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                        
                        <div class="body">
                            <div class="table-responsive">
                                <table class="table table-bordered table-striped table-hover dataTable js-exportable">
                                    <thead>
                                        <tr>
                                            <th>SLNO</th>
                                            
                                            <th>VALIDATION RULE NAME</th>
                                            <th>ACTIVE</th>
                                            <th>VALIDATION RULE DESCRIPTION</th>
                                        </tr>
                                    </thead>
                                    <tfoot>
                                        <tr>
                                            <th>SLNO</th>
                                            
                                            <th>VALIDATION RULE NAME</th>
                                            <th>ACTIVE</th>
                                            <th>VALIDATION RULE DESCRIPTION</th>
                                        </tr>
                                    </tfoot>
                                    
                                    <tbody>
                                    <c:forEach items="${vrulelist}" var="vrule" varStatus="loop">
                                       
         
                                        <tr>
                                            <td>${loop.index+1}</td>
                                            
                                            <td>${vrule.Name}</td>
                                            <td>${vrule.Active}</td>
                                            <td>${vrule.Desc}</td>
                                            
                                        </tr>
                                        
                                     </c:forEach>
                                    </tbody>
                                    
                                </table>
                            <div class="btn-group" style="float: right;">
                            <div><a href="<%=request.getContextPath()%>/standard_object_dataloader"><button class="btn btn-warning">Close</button></a>
                            <button id="vrb" class="btn btn-primary">Proceed</button></div>
                            </div>
                            </div>
                           
                        </div>
                    </div>
                </div>
            </div>
            <!-- #END# Exportable Table -->
   
   
             
            
             <!-- Exportable Table -->
            <div id="wr" class="row clearfix">
                <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                    <div class="card">
                        <div class="header">
                            <h2 style="color:green">
                            
                              <%=request.getAttribute("name").toString().toUpperCase() %> OBJECT WORKFLOW RULE
                            </h2>
                            <h4>Total Workflow Rule=<%=request.getAttribute("wsize") %></h4>
                            <ul class="header-dropdown m-r--5">
                                <li class="dropdown">
                                    <a href="javascript:void(0);" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                                        <i class="material-icons">more_vert</i>
                                    </a>
                                    <ul class="dropdown-menu pull-right">
                                        <li><a href="javascript:void(0);">Action</a></li>
                                        <li><a href="javascript:void(0);">Another action</a></li>
                                        <li><a href="javascript:void(0);">Something else here</a></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                        
                        <div class="body">
                            <div class="table-responsive">
                                <table class="table table-bordered table-striped table-hover dataTable js-exportable">
                                    <thead>
                                        <tr>
                                            <th>SLNO</th>
                                            
                                            <th>WORKFLOW RULE NAME</th>
                                            
                                        </tr>
                                    </thead>
                                    <tfoot>
                                        <tr>
                                            <th>SLNO</th>
                                            
                                            <th>WORKFLOW RULE NAME</th>
                                            
                                            
                                        </tr>
                                    </tfoot>
                                    
                                    <tbody>
                                    <c:forEach items="${wrulelist}" var="vrule" varStatus="loop">
                                       
         
                                        <tr>
                                            <td>${loop.index+1}</td>
                                            
                                            <td>${vrule.Name}</td>
                                           
                                            
                                            
                                        </tr>
                                        
                                     </c:forEach>
                                    </tbody>
                                    
                                </table>
                            <div class="btn-group" style="float: right;">
                            <div><a href="<%=request.getContextPath()%>/standard_object_dataloader"><button class="btn btn-warning">Close</button></a>
                            <button id="wrb" class="btn btn-primary">Proceed</button></div>
                            </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
                      <!-- Exportable Table -->
            <div id="tr" class="row clearfix">
                <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
                    <div class="card">
                        <div class="header">
                            <h2 style="color:green">
                            
                              <%=request.getAttribute("name").toString().toUpperCase() %> OBJECT TRIGGER
                            </h2>
                            <h4>Total Trigger=<%=request.getAttribute("trsize") %></h4>
                            <ul class="header-dropdown m-r--5">
                                <li class="dropdown">
                                    <a href="javascript:void(0);" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                                        <i class="material-icons">more_vert</i>
                                    </a>
                                    <ul class="dropdown-menu pull-right">
                                        <li><a href="javascript:void(0);">Action</a></li>
                                        <li><a href="javascript:void(0);">Another action</a></li>
                                        <li><a href="javascript:void(0);">Something else here</a></li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                        
                        <div class="body">
                            <div class="table-responsive">
                                <table class="table table-bordered table-striped table-hover dataTable js-exportable">
                                    <thead>
                                        <tr>
                                            <th>SLNO</th>
                                            
                                            <th>TRIGGER NAME</th>
                                            
                                        </tr>
                                    </thead>
                                    <tfoot>
                                        <tr>
                                            <th>SLNO</th>
                                            
                                            <th>TRIGGER NAME</th>
                                            
                                            
                                        </tr>
                                    </tfoot>
                                    
                                    <tbody>
                                    <c:forEach items="${trlist}" var="vrule" varStatus="loop">
                                       
         
                                        <tr>
                                            <td>${loop.index+1}</td>
                                            
                                            <td>${vrule.Name}</td>
                                            
                                            
                                            
                                        </tr>
                                        
                                     </c:forEach>
                                    </tbody>
                                    
                                </table>
                            <div class="btn-group" style="float: right;">
                            <div><a href="<%=request.getContextPath()%>/standard_object_dataloader"><button class="btn btn-warning">Close</button></a>
                            
                            <button id="trb" class="btn btn-primary">Proceed</button></div>
                            
                            </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="login-box">
        
        <div id="file" class="card">
            <div class="body" style="text-align: center">
                
                    <div class="msg">Please select an operation to proceed</div>
                    
                    <br>
                    <div class="row">
                    <div class="btn-group">    
                        
                            <div><a href="<%=request.getContextPath()%>/standard_object_bulk_upload_insert_display"><button id="loginbtn" class="btn btn-primary" type="submit">INSERT</button></a>
                            <a href="<%=request.getContextPath()%>/standard_object_bulk_upload_update_display"><button id="loginbtn" class="btn btn-primary" type="submit">UPDATE</button></a>
                            <a href="<%=request.getContextPath()%>/standard_object_bulk_upload_delete_display"><button id="loginbtn" class="btn btn-primary" type="submit">DELETE</button></a></div>
                            <!-- <a href="#"><button id="loginbtn" class="btn btn-primary" type="submit">DELETE</button></a></div> -->
                    
                    </div>    
                    </div>
                    
               
                    
                        
                        <div class="btn-group" style="float: right;">
                            
                            <a href="<%=request.getContextPath()%>/standard_object_dataloader"><button class="btn btn-warning">Close</button></a>
                            
                        </div>
                       <br>
            </div>
        </div>
     
    </div>
  
      <%-- <div id="file" class="card">
       <div class="body">
     <form action="standard_object_bulk_upload" method="POST" enctype="multipart/form-data">
                    <h4>File Upload</h4>
                    <div class="input-group">
                        <span class="input-group-addon">
                            <i class="material-icons">input</i>
                        </span>
                        <div class="form-line">
                            <input type="text" class="form-control" name="batch" placeholder="Batch Size" autofocus>
                        </div>
                    </div>
                    <div class="input-group">
                        <span class="input-group-addon">
                            <i class="material-icons">input</i>
                        </span>
                        <div class="form-line">
                            <input type="file" class="form-control" name="file" placeholder="Upload File">
                        </div>
                    </div>
                    <div class="row">
                    
                    
                   <div class="col-xs-4">
                    
                    <button id="" type="submit" class="btn btn-primary">Upload</button>
                    
                   </div>
                    </div>
</form>

<div class="btn-group" style="float: right;">
<a href="<%=request.getContextPath()%>/standard_object_dataloader"><button class="btn btn-warning">Close</button></a>
</div>
<br>
</div>   
</div> --%>


   <script>
   $(document).ready(
		    function(){
		    	$("#wr").hide();
		    	$("#tr").hide();
		    	$("#file").hide();
		        $("#vrb").click(function () {
		            $("#wr").show();
		            $("#vr").hide();
		            $("#tr").hide();
		            $("#file").hide();
		            //alert('ok');
		        });
		        $("#wrb").click(function () {
		            $("#tr").show();
		            $("#vr").hide();
		            $("#wr").hide();
		            $("#file").hide();
		            //alert('ok');
		        });
		        $("#trb").click(function () {
		            $("#tr").hide();
		            $("#vr").hide();
		            $("#wr").hide();
		            $("#file").show();
		            //alert('ok');
		        });
		        
		    });
   </script>       
            <script src="assets/plugins/jquery-datatable/jquery.dataTables.js"></script>
    <!-- <script src="assets/plugins/jquery-datatable/skin/bootstrap/js/dataTables.bootstrap.js"></script>
    <script src="assets/plugins/jquery-datatable/extensions/export/dataTables.buttons.min.js"></script> -->
    <script src="assets/plugins/jquery-datatable/extensions/export/buttons.flash.min.js"></script>
    <script src="assets/plugins/jquery-datatable/extensions/export/jszip.min.js"></script>
    <script src="assets/plugins/jquery-datatable/extensions/export/pdfmake.min.js"></script>
    <script src="assets/plugins/jquery-datatable/extensions/export/vfs_fonts.js"></script>
    <script src="assets/plugins/jquery-datatable/extensions/export/buttons.html5.min.js"></script>
    <script src="assets/plugins/jquery-datatable/extensions/export/buttons.print.min.js"></script>
