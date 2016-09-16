Feature: Contacts test
  As a user of contacts repository
  I want to crud contacts
  So that I can expose contacts service


  Scenario Outline: search contacts without cdi
    Given we have a list of contacts2
    When we search contacts by name "<name>"2
    Then we should find <result> contacts2

    Examples: examples1
      | name     | result |
      | delta    | 1      |
      | sp       | 2      |
      | querydsl | 1      |
      | abcd     | 0      |


  Scenario: delete a contact without cdi

    Given we have a list of contacts2
    When we delete contact by id 1 2
    Then we should not find contact 1 2