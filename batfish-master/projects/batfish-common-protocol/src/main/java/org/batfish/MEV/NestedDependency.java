package org.batfish.MEV;



import java.util.HashSet;
import java.util.Set;

public class NestedDependency {
    public Set<Set<Dependency>> _nestedDependency;

    public NestedDependency()
    {
        this._nestedDependency = new HashSet<>();
    }

    public Set<Set<Dependency>> getNestedDependency()
    {
        return this._nestedDependency;
    }


    public void orDependencies(Set<Set<Dependency>> orDependency)
    {
        this._nestedDependency.addAll(orDependency);
    }

    public void orDependency(Set<Dependency> orDependency)
    {
        this._nestedDependency.add(orDependency);
    }






    public void andDependency(Set<Set<Dependency>> andDependency)
    {
        if (this._nestedDependency.size() == 0)
        {
            this._nestedDependency.addAll(andDependency);
        } else {
            Set<Set<Dependency>> newNestedDependency = new HashSet<>();
            for (Set<Dependency> andList : andDependency)
            {
                for (Set<Dependency> nestedList : this._nestedDependency)
                {
                    Set<Dependency> tempDependencyList = new HashSet<>();
                    tempDependencyList.addAll(andList);
                    tempDependencyList.addAll(nestedList);
                    newNestedDependency.add(tempDependencyList);
                }
            }
            this._nestedDependency = newNestedDependency;
        }
    }





    public NestedDependency toNewNestedDependency() {
        // 深拷贝 nestedDependency
        NestedDependency nestedDependency = new NestedDependency();
        for (Set<Dependency> list : this._nestedDependency) {
            Set<Dependency> dep = new HashSet<>();
            dep.addAll(list);
            nestedDependency.getNestedDependency().add(dep);
        }
        return nestedDependency;
    }
}
