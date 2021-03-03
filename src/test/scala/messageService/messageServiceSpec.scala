package messageService

import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.json._

import dg.dgService._
import messageService._

object messageServiceSpec {

  def messageServiceSpec =
    suite("messageServiceSpec")(
      
      
      testM("setting up test message schema") {
        for {
          dgService <-  accessDgService
          _         <-  dgService.setSchema("message: string @index(term) .")
          tru       <-  UIO(true)
        } yield assert(tru)(equalTo(true))
      },
      

      testM("store & get message test") {
        for {
          msgService    <-  accessMessageService
          payloadIn     =   "hello world"  
          uid           <-  msgService.storeMessage(payloadIn)
          payloadOut    <-  msgService.getMessage(uid).map(m => m.message)
        } yield assert(payloadIn)(equalTo(payloadOut))
      },


      testM("message exists test") {
        for {
          msgService    <-  accessMessageService
          payloadIn     =   "i exist"
          uid           <-  msgService.storeMessage(payloadIn)
          bool          <-  msgService.messageExists(uid)
        } yield assert(bool)(equalTo(true))
      },
      

      testM("update message test") {
        for {
          msgService    <-  accessMessageService
          payloadIn     =   "old message"
          uid           <-  msgService.storeMessage(payloadIn)
          updatedMessage =  "new message"
          _             <-  msgService.updateMessage(uid, updatedMessage)
          payloadOut    <-  msgService.getMessage(uid).map(m => m.message)
        } yield assert(payloadOut)(equalTo(updatedMessage))
      },


      testM("find messages test") {
        for {
          msgService    <-  accessMessageService
          payloadIn     =   "this message will be searched"
          _             <-  msgService.storeMessage(payloadIn)
          messages      <-  msgService.findMessages("searched")
          payloadOut    =  messages.messages(0).message
        } yield assert(payloadIn)(equalTo(payloadOut))
      },


      testM("delete message test") {
        for {
          msgService    <-  accessMessageService
          payloadIn     =   "this message will be deleted"
          uid           <-  msgService.storeMessage(payloadIn)
          _             <-  msgService.deleteMessage(uid)
          messages      <-  msgService.findMessages("deleted")
        } yield assert(messages.messages)(isEmpty)
      },
      
      
      testM("clearing up test schema") {
        for {
          dgService   <-    accessDgService
          dropped     <-    dgService.dropAll
        } yield assert(dropped)(equalTo(()))
      }
      

  ) @@ sequential
  /* Tests are executed in parallel by default. 
   * If you need to execute them sequentially use the sequential test aspect. 
   * Parallel execution here would result in non deterministic test results */
  

  /* TODO What's the idiomatic way to run a bunch of setup and cleanup effects before & running a suite? is there an example somewhere? eg, in the above case, the setting up of schema and dropping data after testing, are the setup and cleaning effects. Right now, they're put in two fake tests that would always succeed at the top and at the bottom to get the desired result. */
  /* For that create a layer that does your setup and cleanup and use provideLayerShared or use the beforeAll and afterAll test aspects.*/
}
