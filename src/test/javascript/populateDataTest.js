describe("when fetching from directions servlet", function(){

  beforeEach(function() {
    fetch: jasmine.createSpy().and.callFake(function() { return "test" });
  });

  it("display key information from json", function(){
    console.log(fetch());
  });
});
