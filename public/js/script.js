/* Author: PaDa

*/

$(document).ready( function( $ ) {
  var $ApplicationBasketFormular = $("#ApplicationBasketFormular"),
      $InterviewFormular = $("#InterviewFormular"),
          calculateContentContainer = function() {
    var $ApplicationDetailsFormular = $("#ApplicationDetailsFormular"),
          $ApplicationBasketFormular = $("#ApplicationBasketFormular"),
          $main = $("#main"),
          mainHeight = $main.height(),
          contentHeight = mainHeight - 92; // 92px header diff


    $ApplicationBasketFormular.find('.content').css( { height: contentHeight } );
    $ApplicationDetailsFormular.find('.content').css( { height: contentHeight - 20 } ); // 20px padding bottom
  };

  if( $ApplicationBasketFormular.length > 0 ) {
    $(window).resize( calculateContentContainer );
    calculateContentContainer();

    $('#ApplicationBasket').dataTable( {
      "sDom": 'Rrp'
    } );

    var $searchKeyword = $('#searchKeyword'),
        $searchButton = $('#SearchButton'),
        $ApplicationDetailsFormular = $("#ApplicationDetailsFormular"),
        $ApplicationBasketLines = $('#ApplicationBasket tbody tr');

    $(document).keydown(function(event) {
      if ($("#searchKeyword:focus").length > 0) return;
      
      // Down
      if (event.keyCode == '40')
        scrollRow(false);
      // Up
      else if (event.keyCode == '38')
        scrollRow(true);
      // enter
      else if (event.keyCode == '13')
        $('#start-interview-button').click();
      else
        $searchKeyword.focus();
    });

    $ApplicationBasketLines.click( function() {
      var $this = $(this);
      $ApplicationDetailsFormular.addClass("blur");
      $ApplicationBasketLines.removeClass("active");
      $this.addClass("active");
      $('#gender').html( $this.find('.gender').html() );
      $('#firstname').html( $this.find('.firstname').html() );
      $('#familyname').html( $this.find('.familyname').html() );
      $('#applicationNumber').html( $this.find('.applicationNumber').html() );
      $('#fon').html( $this.find('.fon').html() );
      $('#applicationCreatedDate').html( $this.find('.applicationCreatedDate').html() );
      $('#interviewDate').html( $this.find('.interviewDate').html() );
      $ApplicationBasketFormular.addClass("small");
      $ApplicationDetailsFormular.removeClass("hidden");
      setTimeout("$('#ApplicationDetailsFormular').removeClass('blur');", 200);
    });

    var activeRow = function(index) {
      $('#ApplicationBasket tbody tr:visible:eq(' + index +')').click();
    };

    var scrollRow = function(up) {
      if ($('#ApplicationBasket tbody tr.active:visible').length == 0) activeRow(0);

      $('#ApplicationBasket tbody tr:visible').each(function(index, element) {
        var $element = $(element);
        if ($element.hasClass('active')) {
          activeRow(up ? index - 1 : index + 1);
          return false; // break each loop
        }
      });
    };

    var searchFunction = function(instant) {
      var keyWord = $searchKeyword.val();
      
      if (!keyWord || keyWord.length==0) {
        $ApplicationBasketLines.show();
        return;
      }
      $ApplicationBasketLines.hide();
      $('#ApplicationBasket tbody tr td div').each(function(index, element) {
        var $element = $(element);
        if ($element.html().toUpperCase().indexOf(keyWord.toUpperCase()) != -1) {
          $element.parent().parent().show();
        }
      });
      if (!instant)
        return false;
    }; 
    $searchButton.click(function() {
      return searchFunction(false);
    });
    // instant search
    $searchKeyword.keyup(function() {
      searchFunction(true);
    });

  } // init from $ApplicationBasketFormular

  if ( $InterviewFormular.length > 0 ) {
    var $InterviewComment = $('#InterviewComment'),
        $InterviewCommentDialog = $('#InterviewCommentDialog'),
        messageDialogOptions = { draggable : true,
                                 resizable : true,
                                 modal     : true,
                                 width     : 655,
                                 dialogClass : 'commentInterviewDialog',
                                 buttons   : { 'Abbrechen': function() { $(this).dialog("close"); }, 'Ok': function() { $(this).dialog("close"); } }
                               };

    $InterviewComment.click(function() {
      //$InterviewCommentDialog.attr('title', $InterviewComment.html());
      $InterviewCommentDialog.dialog(messageDialogOptions);
      return false; // do not submit formular
    });

    var newSlider = function(inputId, min, max) {
      var $input = $('#' + inputId);
      var $slider = $('#' + inputId + '-slider');
      $slider.slider( { min: min,
                        max: max,
                        animate: true,
                        value : $input.val() ? parseInt($input.val()) : min,
                        slide: function(event, ui) {
                          if (ui.value) $input.val(ui.value);
                        }
      });
      // add listener to input
      var syncValue = function() {
        var new_value = parseInt($input.val()); // value of input
        $slider.slider('option', 'value', new_value);
        return this;
      };
      $input.keyup(syncValue);
      $input.change(syncValue);
    };

    newSlider('Height', 100, 250);
    newSlider('Weight', 50, 120);

  } // init from $InterviewFormular


} );


