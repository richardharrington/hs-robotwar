/*

Queue.js

A function to represent a queue

Created by Stephen Morley - http://code.stephenmorley.org/ - and released under
the terms of the CC0 1.0 Universal legal code:

http://creativecommons.org/publicdomain/zero/1.0/legalcode

*/

function Queue(){
  var queue  = [];
  var offset = 0;

  this.getLength = function(){
    return (queue.length - offset);
  }

  this.isEmpty = function(){
    return (queue.length == 0);
  }

  this.enqueue = function(item){
    queue.push(item);
  }

  this.enqueueArray = function(arr) {
    queue = queue.concat(arr);
  }
  
  this.dequeue = function(){

    // if the queue is empty, return undefined
    if (queue.length == 0) return undefined;

    // store the item at the front of the queue
    var item = queue[offset];

    // increment the offset and remove the free space if necessary
    if (++ offset * 2 >= queue.length){
      queue  = queue.slice(offset);
      offset = 0;
    }
    return item;

  }

  this.drop = function() {
      // this function is like dequeue, but returns nothing.
      // Maybe it's slightly faster.
      
      // if the queue is empty, return undefined
      if (queue.length == 0) return;

      // increment the offset and remove free space if necessary
      if (++ offset * 2 >= queue.length) {
          queue  = queue.slice(offset);
          offset = 0;
      }
  }
  
  this.dropMulti = function(n) {
  
      // if they're asking to remove zero items, or there
      // are zero items in the queue, return undefined.
      if (n == 0 || queue.length == 0) return undefined;

      // update the offset and remove the free space if necessary
      offset = offset + n;
      if (offset * 2 >= queue.length) {
          queue = queue.slice(offset);
          offset = 0;
      }
  }

  this.peek = function(){
    return (queue.length > 0 ? queue[offset] : undefined);
  }

}
