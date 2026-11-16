from src.models import Graph
from src.search import ucs,astar
def graph():
 g=Graph();g.add_edge('A','B',2);g.add_edge('A','C',1);g.add_edge('C','B',0.5);g.add_edge('B','D',1);return g
def test_ucs_optimal():assert ucs(graph(),'A','D')['cost']==2.5
def test_astar_matches_ucs():assert astar(graph(),'A','D')['cost']==ucs(graph(),'A','D')['cost']
def test_no_path():
 g=Graph();g.add_edge('A','B',1);g.add_node('Z');assert ucs(g,'A','Z')['path']==[]
def test_astar_handles_repeated_state_with_a_better_cost():
 g=Graph();g.add_edge('A','B',8);g.add_edge('A','C',1);g.add_edge('C','B',1);g.add_edge('B','D',1);g.add_edge('C','D',9)
 assert astar(g,'A','D')['cost']==3
def test_start_equals_goal():
 result=ucs(graph(),'A','A')
 assert result['path']==['A'] and result['cost']==0
def test_invalid_endpoints_raise():
 import pytest
 with pytest.raises(ValueError):ucs(graph(),'missing','A')
def test_graph_rejects_negative_or_non_finite_edges():
 import pytest
 for cost in (-1,float('inf'),float('nan')):
  with pytest.raises(ValueError):Graph().add_edge('A','B',cost)
