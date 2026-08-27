# Architecture / API / SOAP / Contracts with WSDL & XSD

## 1. WSDL Anatomy

A WSDL document is the machine-readable contract: `types` (XSD schema for messages), `message` parts, `portType` (the abstract operations), `binding` (protocol and encoding), and `service` (the endpoint URL). Tooling reads it to generate client stubs and server skeletons, so the contract -- not hand-written code -- is authoritative.

```xml
<portType name="OrderPort">
  <operation name="getOrder">
    <input message="tns:getOrderRequest"/>
    <output message="tns:getOrderResponse"/>
  </operation>
</portType>
```

## 2. Contract-First vs Code-First

Contract-first authors the WSDL and XSD by hand, then generates code -- the contract is stable, language-neutral, and reviewable by both sides before implementation. Code-first generates the WSDL from annotated classes, which is faster but lets refactors silently change the contract. Enterprise integrations almost always mandate contract-first.

```
contract-first: order.wsdl        -> wsimport -> Java stubs
code-first:     @WebService class -> runtime-generated ?wsdl
```

## 3. XSD Schema Validation

The XSD in `types` defines every element, its data type, cardinality, and allowed values. The SOAP stack validates messages against it automatically, rejecting malformed requests before they reach business logic. This pushes a whole class of input checks into the platform instead of application code.

```xml
<xs:element name="id" type="xs:long"/>
<xs:element name="priority">
  <xs:simpleType>  <!-- enumeration LOW | NORMAL | HIGH -->
```
