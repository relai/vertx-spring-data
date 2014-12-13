  $(function() {
      	
      	$("div .tile").click(function(event){
      		var dest = this.id;
      		if (dest === "api") dest = "api/tasks";
      		window.location.assign("/"+dest);
      	});
      	
   });